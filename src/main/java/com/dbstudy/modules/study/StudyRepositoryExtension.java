package com.dbstudy.modules.study;

import com.dbstudy.modules.tag.Tag;
import com.dbstudy.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface StudyRepositoryExtension {
    Page<Study> findByKeyword(String keyword, Pageable pageable);

    List<Study> findByAccount(Set<Zone> zones, Set<Tag> tags);
}
