package com.fortnox.wh.transactions.v1;

import com.fortnox.wh.items.v1.ItemEntity;
import com.fortnox.wh.sequences.v1.SequenceEntity.SequenceType;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

public class InboundTransactionEntity {

    private static final Integer idZeroPadding = 12;

    public enum Status{PENDING, APPROVED}

    private int dbId;
    @Valid
    @Size(max = 14)
    private String inboundId;
    @Valid
    @NotNull(message = "Item is mandatory")
    @Size(max = 50)
    private String itemId;
    @Valid
    @Size(max = 50)
    private String batchId;
    @Valid
    @Size(max = 50)
    private String stocklocationId;
    @Valid
    @NotNull(message = "Quantity is mandatory")
    @Min(value = 1, message = "Quantity is mandatory")
    private int numAvailableItems;
    private int numReservedItems;
    private Status status;
    private Date created;
    private Date lastModified;
    private ItemEntity item;

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public String getInboundId() {
        return inboundId;
    }

    public void setInboundId(String inboundId) {
        this.inboundId = inboundId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getStocklocationId() {
        return stocklocationId;
    }

    public void setStocklocationId(String stocklocationId) {
        this.stocklocationId = stocklocationId;
    }

    public int getNumAvailableItems() {
        return numAvailableItems;
    }

    public void setNumAvailableItems(int numAvailableItems) {
        this.numAvailableItems = numAvailableItems;
    }

    public int getNumReservedItems() {
        return numReservedItems;
    }

    public void setNumReservedItems(int numReservedItems) {
        this.numReservedItems = numReservedItems;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public ItemEntity getItem() {
        return item;
    }

    public void setItem(ItemEntity item) {
        this.item = item;
    }

    public String generateInboundId(Integer nbr) {
        return String.format("%s_%0"+ idZeroPadding +"d", SequenceType.I, nbr);
    }
}
