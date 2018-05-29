package io.tabletoptools.hawthorne.model;

public class StaticAmount implements Amount {

    private Long amount;

    public StaticAmount(Long amount) {
        this.amount = amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    @Override
    public Long getAmount() {
        return this.amount;
    }
}
