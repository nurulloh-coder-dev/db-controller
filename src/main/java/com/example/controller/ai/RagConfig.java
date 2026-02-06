package com.example.controller.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class RagConfig {
    private final Path docsFolder;
    private final Path vectorStoreFolder;

    public RagConfig(@Value("${app.docs-folder}") String docsFolderPath) {
        this.docsFolder = Path.of(docsFolderPath);
        this.vectorStoreFolder = this.docsFolder.resolve("vectorstore");
    }

    // Vector store file name
    private final String vectorStoreName = "app-knowledge.json";

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) throws Exception {

        // Ensure the vector store folder exists
        Files.createDirectories(vectorStoreFolder);

        // Full path to vector store file
        File vectorStoreFile = vectorStoreFolder.resolve(vectorStoreName).toFile();

        // Initialize the vector store
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();

        if (vectorStoreFile.exists()) {
            simpleVectorStore.load(vectorStoreFile);
        } else {
            // Read all TXT files from the docs folder
            List<Document> documents = new ArrayList<>();
            Files.list(docsFolder)
                    .filter(f -> f.toString().endsWith(".txt"))
                    .forEach(file -> {
                        Resource resource = new FileSystemResource(file.toFile());
                        TextReader reader = new TextReader(resource);
                        reader.getCustomMetadata().put("source", file.getFileName().toString());
                        documents.addAll(reader.get());
                    });

            // Split documents into chunks suitable for embeddings
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocuments = splitter.apply(documents);

            // Add to vector store and save
            simpleVectorStore.add(splitDocuments);
            simpleVectorStore.save(vectorStoreFile);
        }
        log.info("loaded :{}", vectorStoreFile.getAbsolutePath());
        return simpleVectorStore;
    }
}
