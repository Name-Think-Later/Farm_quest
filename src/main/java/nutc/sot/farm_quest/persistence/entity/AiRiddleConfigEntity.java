package nutc.sot.farm_quest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_riddle_config")
public class AiRiddleConfigEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id", nullable = false, unique = true)
    private QuestEntity quest;

    @Column(name = "riddle_prompt", nullable = false)
    private String riddlePrompt;

    @Column(name = "answer_criteria", nullable = false)
    private String answerCriteria;

    @Column(name = "spoiler_policy", nullable = false)
    private String spoilerPolicy;

    @Column(name = "completion_policy", nullable = false)
    private String completionPolicy;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public QuestEntity getQuest() { return quest; }
    public void setQuest(QuestEntity quest) { this.quest = quest; }
    public String getRiddlePrompt() { return riddlePrompt; }
    public void setRiddlePrompt(String riddlePrompt) { this.riddlePrompt = riddlePrompt; }
    public String getAnswerCriteria() { return answerCriteria; }
    public void setAnswerCriteria(String answerCriteria) { this.answerCriteria = answerCriteria; }
    public String getSpoilerPolicy() { return spoilerPolicy; }
    public void setSpoilerPolicy(String spoilerPolicy) { this.spoilerPolicy = spoilerPolicy; }
    public String getCompletionPolicy() { return completionPolicy; }
    public void setCompletionPolicy(String completionPolicy) { this.completionPolicy = completionPolicy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
