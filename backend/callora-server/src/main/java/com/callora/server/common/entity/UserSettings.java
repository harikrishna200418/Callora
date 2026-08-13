package com.callora.server.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "battery_protection_enabled")
    private boolean batteryProtectionEnabled = true;

    @Column(name = "automatic_call_end_enabled")
    private boolean automaticCallEndEnabled = true;

    @Column(name = "critical_battery_threshold")
    private int criticalBatteryThreshold = 7;

    @Column(name = "warning_battery_threshold")
    private int warningBatteryThreshold = 10;

    @Column(name = "read_receipts_enabled")
    private boolean readReceiptsEnabled = true;

    @Column(name = "last_seen_visibility")
    private String lastSeenVisibility = "EVERYONE";

    @Column(name = "profile_visibility")
    private String profileVisibility = "EVERYONE";
}
