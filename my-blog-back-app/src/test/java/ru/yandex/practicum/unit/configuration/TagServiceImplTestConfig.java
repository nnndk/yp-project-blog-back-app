package ru.yandex.practicum.unit.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.repository.post.tag.TagRepository;
import ru.yandex.practicum.service.post.provider.PostProviderService;
import ru.yandex.practicum.service.post.provider.PostProviderServiceImpl;
import ru.yandex.practicum.service.post.tag.TagService;
import ru.yandex.practicum.service.post.tag.TagServiceImpl;
import ru.yandex.practicum.tools.ArrayDiffTools;

@Configuration
public class TagServiceImplTestConfig {

    @Bean
    public ArrayDiffTools arrayDiffTools() {
        return new ArrayDiffTools();
    }

    @Bean
    public TagService tagService(
            TagRepository tagRepository,
            ArrayDiffTools arrayDiffTools
    ) {
        return new TagServiceImpl(tagRepository, arrayDiffTools);
    }
}
