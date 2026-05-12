package com.ticketsystem.service;

import com.ticketsystem.model.Comment;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.repository.CommentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for ticket comments.
 */
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService() {
        this.commentRepository = new CommentRepository();
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public Comment addComment(Ticket ticket, String authorName, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Treść komentarza nie może być pusta.");
        }
        Comment comment = new Comment(ticket, authorName, content);
        return commentRepository.save(comment);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<Comment> getCommentsForTicket(Ticket ticket) {
        return commentRepository.findByTicket(ticket);
    }

    public Optional<Comment> findById(UUID id) {
        return commentRepository.findById(id);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public Comment updateContent(Comment comment, String newContent) {
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("Treść komentarza nie może być pusta.");
        }
        comment.setContent(newContent);
        return commentRepository.update(comment);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteComment(Comment comment) {
        commentRepository.delete(comment);
    }
}
