package org.example.android.controller;

import org.example.android.common.R;
import org.example.android.entity.Post;
import org.example.android.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("/add")
    public R<Post> createPost(@RequestBody Post post) {
        Post createdPost = postService.createPost(post);
        return R.success(createdPost);
    }

    // 添加获取所有帖子的GET端点
    @GetMapping("/all")
    public R<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return R.success(posts);
    }

    // Other controller endpoints...
}