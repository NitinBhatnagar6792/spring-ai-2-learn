package com.nb.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nb.ai.entities.SimpleChatMemory;

@Repository
public interface SimpleChatMemoryRepository extends JpaRepository<SimpleChatMemory, Long>{

	List<SimpleChatMemory> findAllByConversationIdOrderByIdAsc(
	        String conversationId);

	long deleteByConversationId(String conversationId);

}
