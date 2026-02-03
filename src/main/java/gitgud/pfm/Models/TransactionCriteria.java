package gitgud.pfm.Models;


public class TransactionCriteria {
     private String transactionId;

    private Double minAmount;
    private Double maxAmount;
    private String categoryId;
    private String walletId;
    private String dateFrom;
    private String dateTo;
    private Double income;

    public TransactionCriteria() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Double getMinAmount() { return minAmount; }
    public void setMinAmount(Double minAmount) { this.minAmount = minAmount; }

    public Double getMaxAmount() { return maxAmount; }
    public void setMaxAmount(Double maxAmount) { this.maxAmount = maxAmount; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getWalletId() { return walletId; }
    public void setWalletId(String walletId) { this.walletId = walletId; }

    public Double getIncome() { return income; }
    public void setIncome(Double income) { this.income = income; }

    public String getDateFrom() { return dateFrom; }
    public void setDateFrom(String dateFrom) { this.dateFrom = dateFrom; }

    public String getDateTo() { return dateTo; }
    public void setDateTo(String dateTo) { this.dateTo = dateTo; }

    public boolean hasSearchTerm() {
        return transactionId != null && !transactionId.isEmpty();
    }

    public boolean hasFilters() {
        return minAmount != null ||
                income != null ||
               maxAmount != null ||
               (categoryId != null && !categoryId.isEmpty()) ||
               (walletId != null && !walletId.isEmpty()) ||
               (dateFrom != null && !dateFrom.isEmpty()) ||
               (dateTo != null && !dateTo.isEmpty()) 
                ;
    }

    public static class Builder {
        private final TransactionCriteria criteria = new TransactionCriteria();

        public Builder transactionId(String transactionId) {
            criteria.setTransactionId(transactionId);
            return this;
        }

        public Builder minAmount(Double minAmount) {
            criteria.setMinAmount(minAmount);
            return this;
        }

        public Builder maxAmount(Double maxAmount) {
            criteria.setMaxAmount(maxAmount);
            return this;
        }

        public Builder categoryId(String categoryId) {
            criteria.setCategoryId(categoryId);
            return this;
        }

        public Builder walletId(String walletId) {
            criteria.setWalletId(walletId);
            return this;
        }
        public Builder income(Double income) {
            criteria.income = income;
            return this;
        }
        public Builder dateFrom(String dateFrom) {
            criteria.setDateFrom(dateFrom);
            return this;
        }

        public Builder dateTo(String dateTo) {
            criteria.setDateTo(dateTo);
            return this;
        }

        public TransactionCriteria build() {
            return criteria;
        }
    }
}
