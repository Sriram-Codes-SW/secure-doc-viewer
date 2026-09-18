package com.example.securedocviewer.document;

import com.example.securedocviewer.account.AppUser;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility;

    @Column(name = "page_count", nullable = false)
    private int pageCount;

    @Column(name = "tile_size", nullable = false)
    private int tileSize;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "document_page", joinColumns = @JoinColumn(name = "document_id"))
    @OrderBy("pageIndex")
    private List<DocumentPage> pages = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "document_share",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<AppUser> sharedWith = new HashSet<>();

    protected Document() {
    }

    public Document(String id, String title, AppUser owner, Visibility visibility, int tileSize, List<DocumentPage> pages) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.visibility = visibility;
        this.createdAt = Instant.now();
        replacePages(tileSize, pages);
    }

    /** Swaps in a newly rendered set of pages, e.g. after the PDF is replaced. */
    public void replacePages(int tileSize, List<DocumentPage> newPages) {
        this.tileSize = tileSize;
        this.pages.clear();
        this.pages.addAll(newPages);
        this.pageCount = newPages.size();
        touch();
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        touch();
    }

    public AppUser getOwner() {
        return owner;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
        touch();
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getTileSize() {
        return tileSize;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<DocumentPage> getPages() {
        return pages;
    }

    public Set<AppUser> getSharedWith() {
        return sharedWith;
    }
}
