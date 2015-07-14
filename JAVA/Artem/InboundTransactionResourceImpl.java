package com.fortnox.wh.transactions.v1;

import com.fortnox.auth.Auth;
import com.fortnox.reactivewizard.util.WebException;
import com.fortnox.wh.sequences.v1.SequenceEntity;
import com.fortnox.wh.sequences.v1.Sequence;
import com.fortnox.wh.transactions.dao.InboundTransactionDAO;
import com.fortnox.wh.transactions.v1.InboundTransactionEntity.Status;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;

import javax.inject.Inject;
import java.util.*;

import static com.fortnox.wh.utils.AuthUtil.dbId;
import static com.fortnox.wh.utils.MapUtil.getInboundTransactionEntity;

import com.fortnox.wh.items.v1.ItemEntity;
import com.fortnox.wh.items.v1.ItemsResource;


public class InboundTransactionResourceImpl implements InboundTransactionResource {

    private InboundTransactionDAO dao;
    private ItemsResource itemsResource;
    private Sequence sequences;

    @Inject
    public InboundTransactionResourceImpl(InboundTransactionDAO dao, Sequence sequences, ItemsResource itemsResource) {
        this.dao = dao;
        this.sequences = sequences;
        this.itemsResource = itemsResource;
    }

    @Override
    public Observable<List<InboundTransactionEntity>> list(Auth auth, String ids) {
        if (ids != null && !ids.equals("")) {
            return listByIds(auth, ids);
        }
        return listAll(auth);
    }

    private Observable<List<InboundTransactionEntity>> listByIds(Auth auth, String ids) {
        Integer dbId = dbId(auth);
        List<String> inboundIds = Arrays.asList(ids.split("\\,"));
        return this.setItems(auth, dao.findByIds(dbId, inboundIds));
    }

    private Observable<List<InboundTransactionEntity>> listAll(Auth auth) {
        Integer dbId = dbId(auth);
        return this.setItems(auth, dao.select(dbId));
    }

    private Observable<List<InboundTransactionEntity>> setItems(Auth auth, Observable<InboundTransactionEntity> list) {
        List<Observable<InboundTransactionEntity>> results = new ArrayList<>();
        list.forEach(inbound -> {
            results.add(this.setTransactionItem(auth, inbound));
        });
        return Observable.merge(results).toList();
    }

    private Observable<InboundTransactionEntity> setTransactionItem(Auth auth, InboundTransactionEntity inbound) {
         return this.findItemById(auth, inbound.getItemId()).map(item -> {
             inbound.setItem(item);
             return inbound;
         });
    }

    @Override
    public Observable<List<InboundTransactionEntity>> approve(Auth auth, List<String> inboundIds) {
        // validate
        if (inboundIds == null || inboundIds.isEmpty()) {
            throw new WebException(HttpResponseStatus.BAD_REQUEST, "inbound ids not set");
        }

        Integer dbId = dbId(auth);
        List<Observable<InboundTransactionEntity>> results = new ArrayList<>(inboundIds.size());
        dao.findByIds(dbId, inboundIds).forEach(inbound -> {
            inbound.setStatus(Status.APPROVED);
            results.add(dao.approve(inbound).map(rowsAffected -> inbound));
        });

        return Observable.merge(results).toList();
    }

    @Override
    public Observable<List<InboundTransactionEntity>> storeBunch(Auth auth, List<Map<String, Object>> inboundsMap) {
        // validate
        if (inboundsMap == null || inboundsMap.isEmpty()) {
            throw new WebException(HttpResponseStatus.BAD_REQUEST, "inbounds not set");
        }

        List<Observable<InboundTransactionEntity>> results = new ArrayList<>(inboundsMap.size());
        inboundsMap.forEach(inboundMap -> {
            //todo: is it possible to get List<InboundTransactionEntity> instead of List<LinkedHashMap>?
            results.add(this.store(auth, getInboundTransactionEntity(inboundMap)));
        });
        return Observable.merge(results).toList();
    }

    @Override
    public Observable<Integer> deleteTransaction(Auth auth, String inboundId) {
        Integer dbId = dbId(auth);
        return dao.deleteById(dbId, inboundId);
    }

    @Override
    public Observable<Integer> deleteBunch(Auth auth, List<String> inboundIds) {
        // validate
        if (inboundIds == null || inboundIds.isEmpty()) {
            throw new WebException(HttpResponseStatus.BAD_REQUEST, "inbound ids not set");
        }

        List<Observable<Integer>> results = new ArrayList<>(inboundIds.size());
        inboundIds.forEach(inboundId -> {
            results.add(this.deleteTransaction(auth, inboundId));
        });

        return Observable.merge(results).single();
    }

    private Observable<InboundTransactionEntity> store(Auth auth, InboundTransactionEntity inbound) {
        if (inbound.getInboundId() != null) {
            return this.update(auth, inbound.getInboundId(), inbound);
        }
        return this.create(auth, inbound);
    }

    @Override
    public Observable<InboundTransactionEntity> create(Auth auth, InboundTransactionEntity inbound) {
        Integer dbId = dbId(auth);
        inbound.setDbId(dbId);
        inbound.setStatus(Status.PENDING);
        Observable<ItemEntity> itemObs = this.getItemById(auth, inbound.getItemId());
        return itemObs.flatMap(item -> this.createInternal(inbound));
    }
    
    private Observable<InboundTransactionEntity> createInternal(InboundTransactionEntity inbound) {
        return this.sequences.getNextNumber(inbound.getDbId(), SequenceEntity.SequenceType.I).flatMap(sequence -> {
            inbound.setInboundId(inbound.generateInboundId(sequence.getNbr()));
            return dao.create(inbound).map(rowsAffected -> inbound);
        });
    }

    @Override
    public Observable<InboundTransactionEntity> update(Auth auth, String inboundId, InboundTransactionEntity inbound) {
        Integer dbId = dbId(auth);
        inbound.setDbId(dbId);
        Observable<ItemEntity> itemObs = this.getItemById(auth, inbound.getItemId());
        return itemObs.flatMap(item -> dao.update(inbound).map(rowsAffected -> inbound));
    }

    @Override
    public Observable<InboundTransactionEntity> getTransaction(Auth auth, String inboundId) {
        Integer dbId = dbId(auth);
        return dao.findById(dbId, inboundId).flatMap(inbound -> this.setTransactionItem(auth, inbound)).single().onErrorReturn(e -> {
            throw new WebException(HttpResponseStatus.NOT_FOUND, String.format("Inbound with ID %s not found", inboundId));
        });
    }

    private Observable<ItemEntity> getItemById(Auth auth, String itemId) {
        return itemsResource.findItemById(auth, itemId).map(r -> r.getResult()).single().onErrorReturn(e -> {
            throw new WebException(HttpResponseStatus.NOT_FOUND, String.format("Item with ID %s not found", itemId));
        });
    }

    /*
	 * Find item in customer db, no 404 exception
	 */
    private Observable<ItemEntity> findItemById(Auth auth, String itemId) {
        return itemsResource.findItemById(auth, itemId).lastOrDefault(null).map(r -> r.getResult());
    }

}
