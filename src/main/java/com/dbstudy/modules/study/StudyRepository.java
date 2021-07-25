package com.dbstudy.modules.study;

import com.dbstudy.modules.account.Account;
import com.dbstudy.modules.tag.Tag;
import com.dbstudy.modules.zone.Zone;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryExtension {

    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags","zones","managers","members"}, type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(attributePaths = {"tags","managers"})
    Study findStudyWithTagsByPath(String path); // WithTags 는 무시 => findByPath 와 같은 동작

    @EntityGraph(attributePaths = {"zones","managers"})
    Study findStudyWithZonesByPath(String path); // WithTags 는 무시 => findByPath 와 같은 동작

    @EntityGraph(attributePaths = {"managers"})
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(attributePaths = {"members"})
    Study findStudyWithMembersByPath(String path);

    @EntityGraph(attributePaths = {"zones","tags"})
    Study findStudyWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"members","managers"})
    Study findStudyWithManagersAndMembersById(Long id);

    Study findStudyOnlyByPath(String path);

    @EntityGraph(attributePaths = {"tags","zones"})
    List<Study> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Study> findFirst6ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Study> findFirst6ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}
