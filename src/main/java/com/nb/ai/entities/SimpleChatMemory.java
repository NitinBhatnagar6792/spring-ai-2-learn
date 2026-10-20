package com.nb.ai.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "simple_chat_memory")
public class SimpleChatMemory {

	/**
	 * Id and @GeneratedValue configure the primary key.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * nullable = false correctly expresses the mandatory nature of conversationId 
	 * and messageType in the database schema.
	 */
	@Column(name = "conversation_id", nullable = false)
	private String conversationId;
	
	@Column(name = "message_type", nullable = false)
	private String messageType;
	
	@Column(name = "message_content", columnDefinition = "CLOB")
	private String messageContent;

	/**
	 * updatable = false prevents JPA from including createdAt in normal SQL updates
	 */
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	/**
	 * @PrePersist runs before the entity is inserted into the database.
	 */
	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
	}

	/**
	 * @PreUpdate runs before a JPA-managed entity update is executed.
	 */
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}

