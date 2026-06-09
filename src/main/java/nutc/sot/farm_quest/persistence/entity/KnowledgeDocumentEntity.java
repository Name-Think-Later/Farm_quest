package nutc.sot.farm_quest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "knowledge_document")
public class KnowledgeDocumentEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id")
    private QuestEntity quest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @Column(nullable = false)
    private String title;

    @Column(name = "content")
    private String content;

    @Column(nullable = false)
    private String source;

    @Column(name = "spoiler_level", nullable = false, length = 64)
    private String spoilerLevel;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "embedding_status", nullable = false, length = 32)
    private String embeddingStatus;

    @Column(name = "indexed_at")
    private OffsetDateTime indexedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public GameEntity getGame() { return game; }
    public void setGame(GameEntity game) { this.game = game; }
    public QuestEntity getQuest() { return quest; }
    public void setQuest(QuestEntity quest) { this.quest = quest; }
    public LocationEntity getLocation() { return location; }
    public void setLocation(LocationEntity location) { this.location = location; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSpoilerLevel() { return spoilerLevel; }
    public void setSpoilerLevel(String spoilerLevel) { this.spoilerLevel = spoilerLevel; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getEmbeddingStatus() { return embeddingStatus; }
    public void setEmbeddingStatus(String embeddingStatus) { this.embeddingStatus = embeddingStatus; }
    public OffsetDateTime getIndexedAt() { return indexedAt; }
    public void setIndexedAt(OffsetDateTime indexedAt) { this.indexedAt = indexedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
