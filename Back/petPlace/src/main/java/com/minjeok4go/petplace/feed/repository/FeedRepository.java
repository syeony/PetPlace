
package com.minjeok4go.petplace.feed.repository;
import com.minjeok4go.petplace.feed.dto.FeedListResponse;
import com.minjeok4go.petplace.feed.dto.PopularFeedProjection;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.common.constant.FeedCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface FeedRepository extends JpaRepository<Feed, Long> {
    List<Feed> findByUserId(Long userId);
    //     ✅ Feed + Tag + Comment를 Fetch Join으로 가져오기
//    @Query("SELECT DISTINCT f FROM Feed f " +
//            "LEFT JOIN FETCH f.feedTags ft " +
//            "LEFT JOIN FETCH ft.tag " +
//            "LEFT JOIN FETCH f.comments c " +
//            "LEFT JOIN FETCH c.replies " +
//            "WHERE f.id = :feedId")
//    Optional<Feed> findFeedWithTagsAndComments(@Param("feedId") Long feedId);
    @Query("SELECT f FROM Likes l " +
            "JOIN l.feed f " +
            "WHERE l.user.id = :userId " +
            "AND f.deletedAt IS NULL")
    List<Feed> findLikedFeedsByUserId(Long userId);

    @Query("SELECT f.id FROM Feed f " +
            "WHERE f.userId = :userId " +
            "AND f.createdAt > :afterTime " +
            "ORDER BY f.createdAt DESC")
    List<Long> findTop3IdsByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("afterTime") LocalDateTime afterTime);

    @Query("select f.id from Feed f where f.userId = :uid and f.deletedAt is null")
    List<Long> findIdsByUserId(@Param("uid") Long userId);

    @Query("select f from Feed f where f.id in :ids and f.deletedAt is null")
    List<Feed> findAllActiveByIdIn(@Param("ids") List<Long> ids);

    List<Feed> findTop200ByCreatedAtAfterOrderByLikesDesc(LocalDateTime createdAtAfter, Sort sort);
    Optional<Feed> findByIdAndDeletedAtIsNull(Long id);
    List<Feed> findAllByDeletedAtIsNull();
    Optional<Feed> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
    List<Feed> findByCategory(FeedCategory category);
    // ✅ feedTags로 경로 변경
    @EntityGraph(attributePaths = {"feedTags.tag"})
    List<Feed> findDistinctByFeedTags_Tag_Id(Long tagId);
    List<Feed> findByUserNick(String userNick);
    List<Feed> findTop200ByOrderByLikesDesc();
    @Query("""
select
  f.id            as id,
  f.content       as content,
  f.userId        as userId,
  f.userNick      as userNick,
  f.userImg       as userImg,
  f.regionId      as regionId,
  f.category      as category,
  f.createdAt     as createdAt,
  f.likes         as likes,
  f.views         as views,
  p.id            as petId,
  p.name          as petName,
  p.animal        as petAnimal,
  p.breed         as petBreed
from Feed f
left join com.minjeok4go.petplace.pet.entity.Pet p
  on p.userId = f.userId
where f.deletedAt is null
order by f.likes desc, f.createdAt desc
""")
    Page<PopularFeedProjection> findPopularFeedsWithPets(Pageable pageable);
//    @Query("""
//    SELECT DISTINCT f
//    FROM Feed f
//    JOIN FETCH f.user u
//    LEFT JOIN FETCH u.pets p
//    ORDER BY f.likes DESC, f.createdAt DESC
//""")
//    Page<Feed> findPopularFeedsWithUserAndPets(Pageable pageable);
//}
}