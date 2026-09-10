package com.example.cnwasshu.domain.bookmark.service;

import com.example.cnwasshu.domain.bookmark.dto.BookmarkResponse;
import com.example.cnwasshu.domain.bookmark.entity.Bookmark;
import com.example.cnwasshu.domain.bookmark.entity.BookmarkType;
import com.example.cnwasshu.domain.bookmark.repository.BookmarkRepository;
import com.example.cnwasshu.domain.home.entity.Activity;
import com.example.cnwasshu.domain.home.entity.Restaurant;
import com.example.cnwasshu.domain.home.repository.ActivityRepository;
import com.example.cnwasshu.domain.home.repository.RestaurantRepository;
import com.example.cnwasshu.domain.user.entity.User;
import com.example.cnwasshu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public void addBookmark(
            Long userId,
            BookmarkType type,
            Long targetId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 사용자입니다.")
                );

        switch (type) {
            case ACTIVITY -> addActivityBookmark(user, targetId);
            case RESTAURANT -> addRestaurantBookmark(user, targetId);
            default -> throw new IllegalArgumentException("지원하지 않는 장바구니 타입입니다.");
        }
    }

    private void addActivityBookmark(User user, Long activityId) {

        if (bookmarkRepository.existsByUser_IdAndActivity_Id(
                user.getId(),
                activityId
        )) {
            throw new IllegalArgumentException("이미 장바구니에 담긴 항목입니다.");
        }

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 체험입니다.")
                );

        Bookmark bookmark = new Bookmark(user, activity);

        bookmarkRepository.save(bookmark);
    }

    private void addRestaurantBookmark(User user, Long restaurantId) {

        if (bookmarkRepository.existsByUser_IdAndRestaurant_Id(
                user.getId(),
                restaurantId
        )) {
            throw new IllegalArgumentException("이미 장바구니에 담긴 항목입니다.");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 식당입니다.")
                );

        Bookmark bookmark = new Bookmark(user, restaurant);

        bookmarkRepository.save(bookmark);
    }

    public List<BookmarkResponse> getBookmarks(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        return bookmarkRepository
                .findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(BookmarkResponse::from)
                .toList();
    }

    @Transactional
    public void deleteBookmark(
            Long userId,
            BookmarkType type,
            Long targetId
    ) {
        Bookmark bookmark = switch (type) {
            case ACTIVITY ->
                    bookmarkRepository
                            .findByUser_IdAndActivity_Id(userId, targetId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "장바구니에 존재하지 않는 항목입니다."
                                    )
                            );

            case RESTAURANT ->
                    bookmarkRepository
                            .findByUser_IdAndRestaurant_Id(userId, targetId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "장바구니에 존재하지 않는 항목입니다."
                                    )
                            );

            default ->
                    throw new IllegalArgumentException(
                            "지원하지 않는 장바구니 타입입니다."
                    );
        };

        bookmarkRepository.delete(bookmark);
    }
}