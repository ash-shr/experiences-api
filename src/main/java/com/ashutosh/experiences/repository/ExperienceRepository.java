package com.ashutosh.experiences.repository;

import com.ashutosh.experiences.domain.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data generates the implementation of this interface at runtime. We
 * never write the SQL for findAll/findById/save -- those come from
 * JpaRepository. Methods we declare ourselves are derived from their names:
 * findByCityIgnoreCase becomes "select ... from experience where lower(city) =
 * lower(?)".
 */
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByCityIgnoreCase(String city);
}
