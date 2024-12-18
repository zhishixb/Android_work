package org.example.android.service;

import org.example.android.entity.Post;
import org.example.android.mapper.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public Post createPost(Post post) {
        post.setUpdatetime(java.time.LocalDateTime.now());
        return postRepository.save(post);
    }

    // 添加获取所有帖子的方法
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // Other service methods...
}