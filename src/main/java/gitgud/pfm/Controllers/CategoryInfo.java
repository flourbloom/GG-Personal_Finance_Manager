package gitgud.pfm.Controllers;

/**
 * Shared data class for category information used in transaction dialogs
 */
public class CategoryInfo {
    public String id;
    public String color;
    public String icon;
    
    public CategoryInfo(String id, String color, String icon) {
        this.id = id;
        this.color = color;
        this.icon = icon;
    }
}
