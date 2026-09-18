package com.example.securedocviewer.document;

import com.example.securedocviewer.account.AppUser;
import com.example.securedocviewer.account.Role;
import com.example.securedocviewer.account.AppUserRepository;
import com.example.securedocviewer.account.UserAccountService;
import com.example.securedocviewer.audit.AuditEvent.Actor;
import com.example.securedocviewer.audit.AuditEvent.Subject;
import com.example.securedocviewer.audit.AuditEventType;
import com.example.securedocviewer.audit.AuditLogService;
import com.example.securedocviewer.exception.BadRequestException;
import com.example.securedocviewer.exception.DocumentNotFoundException;
import com.example.securedocviewer.exception.ForbiddenException;
import com.example.securedocviewer.exception.ResourceNotFoundException;
import com.example.securedocviewer.model.PageInfo;
import com.example.securedocviewer.service.TileGenerationService;
import com.example.securedocviewer.service.TileGenerationService.RenderedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Every document access rule lives here:
 * <ul>
 *   <li><b>view</b> — the owner, users it is shared with, everyone if its
 *       visibility is EVERYONE, and admins;</li>
 *   <li><b>manage</b> (rename, change visibility, share, replace, delete) —
 *       admins, and the owner while they still hold the PUBLISHER role (a
 *       publisher demoted to reader keeps read access but can no longer
 *       share, replace or re-publish what they uploaded).</li>
 * </ul>
 * A document the user can't view is reported as not found, never as
 * forbidden, so its existence isn't revealed.
 *
 * <p>Rendering a PDF is slow, so it runs outside any database transaction:
 * render to staging, then commit tiles and metadata.
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final int MAX_TITLE_LENGTH = 200;
    static final java.time.Duration DENIAL_AUDIT_INTERVAL = java.time.Duration.ofSeconds(5);

    private final DocumentRepository documents;
    private final AppUserRepository users;
    private final TileGenerationService tiles;
    private final AuditLogService audit;
    private final TransactionTemplate tx;

    public DocumentService(DocumentRepository documents,
                           AppUserRepository users,
                           TileGenerationService tiles,
                           AuditLogService audit,
                           PlatformTransactionManager transactionManager) {
        this.documents = documents;
        this.users = users;
        this.tiles = tiles;
        this.audit = audit;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public List<DocumentSummary> list(Viewer viewer) {
        return tx.execute(status -> {
            List<Document> visible = viewer.admin()
                    ? documents.findAllWithOwner()
                    : documents.findVisibleTo(viewer.username(), Visibility.EVERYONE);
            return visible.stream().map(d -> summary(d, viewer)).toList();
        });
    }

    public DocumentDetail get(String documentId, Viewer viewer, Actor actor) {
        return tx.execute(status -> detail(requireViewable(documentId, viewer, actor), viewer));
    }

    /** The grid of one page, for issuing tile URLs. */
    public PageInfo requirePage(String documentId, int page, Viewer viewer, Actor actor) {
        return tx.execute(status -> {
            Document document = requireViewable(documentId, viewer, actor);
            return document.getPages().stream()
                    .filter(p -> p.getPageIndex() == page)
                    .findFirst()
                    .map(p -> pageInfo(p, document.getTileSize()))
                    .orElseThrow(() -> new DocumentNotFoundException("No such page."));
        });
    }

    /**
     * The per-tile check, run on every tile request so that unsharing or
     * deleting a document cuts off even tile URLs that were already issued.
     * Returns the title for the audit record.
     */
    public Optional<TileAccess> tileAccessIfViewable(String documentId, Viewer viewer) {
        return viewer.admin()
                ? documents.findTileAccess(documentId)
                : documents.findTileAccessIfVisible(documentId, viewer.username(), Visibility.EVERYONE);
    }

    public DocumentDetail upload(String rawTitle, String originalFilename, InputStream pdf, Visibility visibility,
                                 Viewer viewer, Actor actor) throws IOException {
        String title = validTitle(rawTitle == null || rawTitle.isBlank() ? titleFromFilename(originalFilename) : rawTitle);
        RenderedDocument rendered = tiles.render(pdf);
        String documentId = UUID.randomUUID().toString();
        tiles.commit(rendered, documentId, 1);
        try {
            DocumentDetail created = tx.execute(status -> {
                AppUser owner = users.findByUsername(viewer.username())
                        .orElseThrow(() -> new ResourceNotFoundException("No such user."));
                Document document = documents.save(new Document(documentId, title, owner,
                        visibility == null ? Visibility.PRIVATE : visibility,
                        1, rendered.tileSize(), toPages(rendered)));
                return detail(document, viewer);
            });
            audit.record(AuditEventType.DOCUMENT_UPLOADED, actor,
                    Subject.document(documentId, title, rendered.pages().size() + " pages"));
            return created;
        } catch (RuntimeException e) {
            tiles.deleteTiles(documentId);
            throw e;
        }
    }

    /**
     * Swaps in a new PDF; the document keeps its id, title, visibility and
     * shares. Rendering happens outside any transaction; then, under a row
     * lock, the new render is moved into a fresh version directory and the
     * row switched to it in one transaction. The previous version is removed
     * only after that commits, so readers never see a mix of old and new
     * tiles and concurrent replaces/deletes are serialised.
     */
    public DocumentDetail replaceFile(String documentId, InputStream pdf, Viewer viewer, Actor actor) throws IOException {
        tx.executeWithoutResult(status -> requireManageable(documentId, viewer, actor));
        RenderedDocument rendered = tiles.render(pdf);
        int[] previousVersion = new int[1];
        DocumentDetail updated;
        try {
            updated = tx.execute(status -> {
                Document document = documents.findByIdForUpdate(documentId)
                        .orElseThrow(() -> new DocumentNotFoundException("Document not found."));
                // Rendering can take a while: the owner may have been demoted or disabled,
                // or the document handed to someone else, since the check above.
                if (!canManage(document, currentRoles(viewer))) {
                    recordDenied(actor, viewer, Subject.document(documentId, document.getTitle(), "manage"));
                    throw new ForbiddenException("Only the owner (as a publisher) or an admin can change this document.");
                }
                previousVersion[0] = document.getTileVersion();
                int nextVersion = document.getTileVersion() + 1;
                try {
                    // Under the row lock nothing committed points past the current version,
                    // so anything already at the next one is debris from a failed replace.
                    tiles.deleteVersion(documentId, nextVersion);
                    tiles.commit(rendered, documentId, nextVersion);
                } catch (IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
                document.replacePages(nextVersion, rendered.tileSize(), toPages(rendered));
                return detail(document, viewer);
            });
        } catch (RuntimeException e) {
            tiles.discard(rendered);
            throw e;
        }
        try {
            tiles.deleteVersion(documentId, previousVersion[0]);
        } catch (IOException e) {
            log.warn("Could not remove superseded tiles of {}; the storage janitor will retry", documentId, e);
        }
        audit.record(AuditEventType.DOCUMENT_REPLACED, actor,
                Subject.document(documentId, updated.title(), updated.pageCount() + " pages"));
        return updated;
    }

    public DocumentDetail update(String documentId, String newTitle, Visibility newVisibility,
                                 Viewer viewer, Actor actor) {
        DocumentDetail updated = tx.execute(status -> {
            Document document = requireManageable(documentId, viewer, actor);
            if (newTitle != null) {
                document.setTitle(validTitle(newTitle));
            }
            if (newVisibility != null) {
                document.setVisibility(newVisibility);
            }
            return detail(document, viewer);
        });
        audit.record(AuditEventType.DOCUMENT_UPDATED, actor, Subject.document(documentId, updated.title(),
                "visibility=" + updated.visibility()));
        return updated;
    }

    public void delete(String documentId, Viewer viewer, Actor actor) {
        String title = tx.execute(status -> {
            requireManageable(documentId, viewer, actor);
            // Lock the row so a replace can't commit new tiles into a document being deleted.
            Document document = documents.findByIdForUpdate(documentId)
                    .orElseThrow(() -> new DocumentNotFoundException("Document not found."));
            documents.delete(document);
            return document.getTitle();
        });
        try {
            tiles.deleteTiles(documentId);
        } catch (IOException e) {
            // The record is gone, so the tiles are unreachable; StorageJanitor removes them later.
            log.warn("Could not delete tiles for deleted document {}; the storage janitor will retry", documentId, e);
        }
        audit.record(AuditEventType.DOCUMENT_DELETED, actor, Subject.document(documentId, title));
    }

    /**
     * Admin only: hands a document to another publisher (e.g. its owner left
     * or was demoted). The new owner must be an enabled PUBLISHER or ADMIN.
     */
    public DocumentDetail transferOwnership(String documentId, String rawUsername, Viewer viewer, Actor actor) {
        if (!viewer.admin()) {
            throw new ForbiddenException("Only an admin can change a document's owner.");
        }
        String username = UserAccountService.normalizeUsername(rawUsername);
        String[] previousOwner = new String[1];
        DocumentDetail updated = tx.execute(status -> {
            Document document = requireViewable(documentId, viewer, actor);
            AppUser newOwner = users.findByUsername(username)
                    .orElseThrow(() -> new BadRequestException("No user named '" + username + "'."));
            if (!newOwner.isEnabled() || newOwner.getRole() == com.example.securedocviewer.account.Role.READER) {
                throw new BadRequestException("The new owner must be an enabled publisher or admin.");
            }
            previousOwner[0] = document.getOwner().getUsername();
            document.getSharedWith().removeIf(u -> u.getId().equals(newOwner.getId()));
            document.setOwner(newOwner);
            return detail(document, viewer);
        });
        audit.record(AuditEventType.DOCUMENT_OWNER_CHANGED, actor,
                Subject.document(documentId, updated.title(), previousOwner[0] + " -> " + username));
        return updated;
    }

    public List<String> shares(String documentId, Viewer viewer, Actor actor) {
        return tx.execute(status -> sharedWith(requireManageable(documentId, viewer, actor)));
    }

    public List<String> share(String documentId, String rawUsername, Viewer viewer, Actor actor) {
        String username = UserAccountService.normalizeUsername(rawUsername);
        String[] title = new String[1];
        List<String> result = tx.execute(status -> {
            Document document = requireManageable(documentId, viewer, actor);
            AppUser user = users.findByUsername(username)
                    .orElseThrow(() -> new BadRequestException("No user named '" + username + "'."));
            if (user.getId().equals(document.getOwner().getId())) {
                throw new BadRequestException("The owner always has access.");
            }
            document.getSharedWith().add(user);
            document.touch();
            title[0] = document.getTitle();
            return sharedWith(document);
        });
        audit.record(AuditEventType.DOCUMENT_SHARED, actor, Subject.document(documentId, title[0], "with " + username));
        return result;
    }

    public List<String> unshare(String documentId, String rawUsername, Viewer viewer, Actor actor) {
        String username = UserAccountService.normalizeUsername(rawUsername);
        String[] title = new String[1];
        List<String> result = tx.execute(status -> {
            Document document = requireManageable(documentId, viewer, actor);
            document.getSharedWith().removeIf(u -> u.getUsername().equals(username));
            document.touch();
            title[0] = document.getTitle();
            return sharedWith(document);
        });
        audit.record(AuditEventType.DOCUMENT_UNSHARED, actor, Subject.document(documentId, title[0], "from " + username));
        return result;
    }

    private Document requireViewable(String documentId, Viewer viewer, Actor actor) {
        Document document = documents.findById(documentId).orElse(null);
        if (document == null || !canView(document, viewer)) {
            recordDenied(actor, viewer, Subject.document(documentId, null, "view"));
            throw new DocumentNotFoundException("Document not found.");
        }
        return document;
    }

    private Document requireManageable(String documentId, Viewer viewer, Actor actor) {
        Document document = requireViewable(documentId, viewer, actor);
        if (!canManage(document, viewer)) {
            recordDenied(actor, viewer, Subject.document(documentId, document.getTitle(), "manage"));
            throw new ForbiddenException("Only the owner (as a publisher) or an admin can change this document.");
        }
        return document;
    }

    /**
     * At most one denial per user every few seconds: enough to show probing in
     * the audit trail, without letting any signed-in user grow the audit table
     * at request rate by asking for made-up ids.
     */
    private void recordDenied(Actor actor, Viewer viewer, Subject subject) {
        audit.recordAtMostEvery(DENIAL_AUDIT_INTERVAL, "denied:" + viewer.username(),
                AuditEventType.ACCESS_DENIED, actor, subject);
    }

    private static boolean canView(Document document, Viewer viewer) {
        return viewer.admin()
                || document.getVisibility() == Visibility.EVERYONE
                || document.getOwner().getUsername().equals(viewer.username())
                || document.getSharedWith().stream().anyMatch(u -> u.getUsername().equals(viewer.username()));
    }

    /** The viewer's roles as they are in the database now, not as they were at sign-in. */
    private Viewer currentRoles(Viewer viewer) {
        return users.findByUsername(viewer.username())
                .filter(AppUser::isEnabled)
                .map(user -> new Viewer(user.getUsername(), user.getRole() == Role.ADMIN,
                        user.getRole() == Role.ADMIN || user.getRole() == Role.PUBLISHER))
                .orElse(new Viewer(viewer.username(), false, false));
    }

    private static boolean canManage(Document document, Viewer viewer) {
        return viewer.admin()
                || (viewer.publisher() && document.getOwner().getUsername().equals(viewer.username()));
    }

    private static DocumentSummary summary(Document d, Viewer viewer) {
        boolean manage = canManage(d, viewer);
        return new DocumentSummary(d.getId(), d.getTitle(), d.getPageCount(), d.getOwner().getUsername(),
                d.getVisibility(), d.getCreatedAt().getEpochSecond(), d.getUpdatedAt().getEpochSecond(),
                manage, manage ? d.getSharedWith().size() : null);
    }

    private static DocumentDetail detail(Document d, Viewer viewer) {
        boolean manage = canManage(d, viewer);
        List<PageInfo> pages = d.getPages().stream().map(p -> pageInfo(p, d.getTileSize())).toList();
        return new DocumentDetail(d.getId(), d.getTitle(), d.getPageCount(), d.getOwner().getUsername(),
                d.getVisibility(), d.getCreatedAt().getEpochSecond(), d.getUpdatedAt().getEpochSecond(),
                manage, manage ? sharedWith(d) : null, pages);
    }

    private static List<String> sharedWith(Document d) {
        return d.getSharedWith().stream().map(AppUser::getUsername).sorted(Comparator.naturalOrder()).toList();
    }

    private static PageInfo pageInfo(DocumentPage p, int tileSize) {
        return new PageInfo(p.getPageIndex(), p.getRows(), p.getCols(), tileSize, p.getWidthPx(), p.getHeightPx());
    }

    private static List<DocumentPage> toPages(RenderedDocument rendered) {
        return rendered.pages().stream()
                .map(p -> new DocumentPage(p.page(), p.rows(), p.cols(), p.pageWidthPx(), p.pageHeightPx()))
                .toList();
    }

    private static String validTitle(String title) {
        String trimmed = title == null ? "" : title.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_TITLE_LENGTH) {
            throw new BadRequestException("Title must be 1-" + MAX_TITLE_LENGTH + " characters.");
        }
        return trimmed;
    }

    private static String titleFromFilename(String filename) {
        if (filename == null) {
            return "";
        }
        String name = filename.replaceAll("^.*[\\\\/]", "");
        return name.toLowerCase().endsWith(".pdf") ? name.substring(0, name.length() - 4) : name;
    }
}
