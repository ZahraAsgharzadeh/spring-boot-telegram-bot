package com.telegram.downloader.bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "video", schema = "public")
public class VideoEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private UUID id;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "quality", nullable = false)
    private String quality;

    @Column(name = "video_id", nullable = false, unique = true)
    private String videoId;

    @Column(name = "download_url", columnDefinition="TEXT", nullable = false)
    private String downloadUrl;
}
