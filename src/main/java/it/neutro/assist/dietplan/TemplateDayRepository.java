package it.neutro.assist.dietplan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateDayRepository extends JpaRepository<TemplateDay, Long> {

    List<TemplateDay> findByTemplateIdOrderByDayNumber(Long templateId);
}
