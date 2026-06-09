package nutc.sot.farm_quest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "coupon_usage")
public class CouponUsageEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private CouponEntity coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visitor_account_id", nullable = false)
    private VisitorAccountEntity visitorAccount;

    @Column(name = "used_at", nullable = false)
    private OffsetDateTime usedAt;

    @Column(name = "client_confirmed_at")
    private OffsetDateTime clientConfirmedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public CouponEntity getCoupon() { return coupon; }
    public void setCoupon(CouponEntity coupon) { this.coupon = coupon; }
    public VisitorAccountEntity getVisitorAccount() { return visitorAccount; }
    public void setVisitorAccount(VisitorAccountEntity visitorAccount) { this.visitorAccount = visitorAccount; }
    public OffsetDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(OffsetDateTime usedAt) { this.usedAt = usedAt; }
    public OffsetDateTime getClientConfirmedAt() { return clientConfirmedAt; }
    public void setClientConfirmedAt(OffsetDateTime clientConfirmedAt) { this.clientConfirmedAt = clientConfirmedAt; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
