package com.fortnox.wh.transactions.dao;


import com.fortnox.reactivewizard.db.DAO;
import com.fortnox.reactivewizard.db.Named;
import com.fortnox.reactivewizard.db.Query;
import com.fortnox.reactivewizard.db.Update;

import com.fortnox.wh.transactions.v1.InboundTransactionEntity;
import rx.Observable;

import java.util.List;

public interface InboundTransactionDAO extends DAO {

    @Query("select * from inbound_transactions where db_id = :dbId")
    public Observable<InboundTransactionEntity> select(@Named("dbId") Integer dbId);

    @Query("select * from inbound_transactions where db_id = :dbId and inbound_id=:inboundId")
    public Observable<InboundTransactionEntity> findById(@Named("dbId") Integer dbId, @Named("inboundId") String inboundId);

    @Query("select * from inbound_transactions where db_id = :dbId and inbound_id IN (:inboundIds)")
    public Observable<InboundTransactionEntity> findByIds(@Named("dbId") Integer dbId, @Named("inboundIds") List<String> inboundIds);

    @Update("insert into inbound_transactions (db_id, inbound_id, item_id, num_available_items, num_reserved_items, status, batch_id, stocklocation_id) " +
            "values (:inbound.dbId, :inbound.inboundId, :inbound.itemId, :inbound.numAvailableItems, :inbound.numReservedItems, :inbound.status, :inbound.batchId, :inbound.stocklocationId)")
    Observable<Integer> create(@Named("inbound") InboundTransactionEntity inbound);

    @Update("update inbound_transactions set status =:inbound.status where inbound_id = :inbound.inboundId and db_id = :inbound.dbId")
    Observable<Integer> approve(@Named("inbound") InboundTransactionEntity inbound);

    @Update("update inbound_transactions set " +
            "num_available_items =:inbound.numAvailableItems, " +
            "num_reserved_items =:inbound.numReservedItems, " +
            "stocklocation_id =:inbound.stocklocationId " +
            "where inbound_id = :inbound.inboundId and db_id = :inbound.dbId")
    Observable<Integer> update(@Named("inbound") InboundTransactionEntity inbound);


    @Update("delete from inbound_transactions where db_id = :dbId and inbound_id = :inboundId")
    Observable<Integer> deleteById(@Named("dbId") Integer dbId, @Named("inboundId") String inboundId);


}
