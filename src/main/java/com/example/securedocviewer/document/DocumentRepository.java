package com.example.securedocviewer.document;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, String> {

    /** Everything a non-admin may open: own, shared with them, or visible to everyone. */
    @Query("""
            select distinct d from Document d
            join fetch d.owner
            left join d.sharedWith s
            where d.visibility = :everyone or d.owner.username = :username or s.username = :username
            order by d.createdAt desc
            """)
    List<Document> findVisibleTo(@Param("username") String username, @Param("everyone") Visibility everyone);

    @Query("select d from Document d join fetch d.owner order by d.createdAt desc")
    List<Document> findAllWithOwner();

    /**
     * The per-tile access check: the title (for the audit record) if the user
     * may see the document, empty otherwise. One indexed query, no entity loading.
     */
    @Query("""
            select new com.example.securedocviewer.document.TileAccess(d.title, d.tileVersion) from Document d
            where d.id = :id and (d.visibility = :everyone or d.owner.username = :username
                   or :username in (select s.username from d.sharedWith s))
            """)
    Optional<TileAccess> findTileAccessIfVisible(@Param("id") String id, @Param("username") String username,
                                                 @Param("everyone") Visibility everyone);

    @Query("select new com.example.securedocviewer.document.TileAccess(d.title, d.tileVersion) from Document d where d.id = :id")
    Optional<TileAccess> findTileAccess(@Param("id") String id);

    /** Row lock for replace/delete, so concurrent changes to one document are serialised. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Document d where d.id = :id")
    Optional<Document> findByIdForUpdate(@Param("id") String id);

    /** Current tile version per document, for the storage janitor. */
    @Query("select d.id, d.tileVersion from Document d")
    List<Object[]> findAllTileVersions();

    long countByOwner_Username(String username);

    @Query("select d.id from Document d")
    List<String> findAllIds();
}
