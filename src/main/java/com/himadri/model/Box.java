package com.himadri.model;

import java.util.List;

public class Box {
    private final String image;
    private final String brandImage;
    private final String title;
    private final String category;
    private final int productGroupNb;
    private final List<Article> articles;

    public Box(String image, String brandImage, String title, String category, int productGroupNb, List<Article> articles) {
        this.image = image;
        this.brandImage = brandImage;
        this.title = title;
        this.category = category;
        this.productGroupNb = productGroupNb;
        this.articles = articles;
    }

    public String getImage() {
        return image;
    }

    public String getBrandImage() {
        return brandImage;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public int getProductGroupNb() {
        return productGroupNb;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public static class Article {
        private final String number;
        private final String price;
        private final String description;

        public Article(String number, String price, String description) {
            this.number = number;
            this.price = price;
            this.description = description;
        }

        public String getNumber() {
            return number;
        }

        public String getPrice() {
            return price;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "Article{" +
                    "number='" + number + '\'' +
                    ", price='" + price + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Box{" +
                "image='" + image + '\'' +
                ", brandImage='" + brandImage + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", articles=" + articles +
                '}';
    }
}
