package gitgud.pfm.Models;

import java.util.Objects;

public class Category {
    public enum Type {
        INCOME, EXPENSE
    }

    private String id;
    private String name;
    private String description;
    private Type type; // INCOME or EXPENSE
    // TODO remove custom when categorycontroller is modified to list only default
    private boolean custom;

    public Category() {
    }

    public Category(String id, String name, String description, Type type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    // TODO remove custom when categorycontroller is modified to list only default
    public boolean getCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    // TODO remove custom when categorycontroller is modified to list only default
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", custom=" + custom +
                '}';
    }

    // TODO remove custom when categorycontroller is modified to list only default
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id &&
                custom == category.custom &&
                Objects.equals(name, category.name) &&
                Objects.equals(description, category.description) &&
                type == category.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, type, custom);
    }
}
