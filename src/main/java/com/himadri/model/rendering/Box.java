package com.himadri.model.rendering;

import java.util.List;

public class Box {
    private final String image;
    private final String brandImage;
    private final String title;
    private final String category;
    private final String productGroup;
    private final int indexOfProductGroup;
    private final int width;
    private final int height;
    private int row;
    private int column;
    private final boolean newProduct;
    private final List<Article> articles;

    public Box(String image, String brandImage, String title, String category, String productGroup,
               int indexOfProductGroup, int width, int height, boolean newProduct, List<Article> articles) {
        this.image = image;
        this.brandImage = brandImage;
        this.title = title;
        this.category = category;
        this.productGroup = productGroup;
        this.indexOfProductGroup = indexOfProductGroup;
        this.width = width;
        this.height = height;
        this.newProduct = newProduct;
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

    public String getProductGroup() {
        return productGroup;
    }

    public int getIndexOfProductGroup() {
        return indexOfProductGroup;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void setDimensions(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public boolean isNewProduct() {
        return newProduct;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public static class Article {
        private final String number;
        private final String price;
        private final String description;
        private final String indexName;
        private final boolean emptyItemText;

        public Article(String number, String price, String description, String indexName, boolean emptyItemText) {
            this.number = number;
            this.price = price;
            this.description = description;
            this.indexName = indexName;
            this.emptyItemText = emptyItemText;
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

        public String getIndexName() {
            return indexName;
        }

        public boolean isEmptyItemText() {
            return emptyItemText;
        }

        @Override
        public String toString() {
            return "Article{" +
                    "number='" + number + '\'' +
                    ", price='" + price + '\'' +
                    ", description='" + description + '\'' +
                    ", indexName='" + indexName + '\'' +
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
                ", indexOfProductGroup='" + indexOfProductGroup + '\'' +
                ", width='" + width + '\'' +
                ", height='" + height + '\'' +
                ", row='" + row + '\'' +
                ", column='" + column + '\'' +
                ", articles=" + articles +
                '}';
    }
}
