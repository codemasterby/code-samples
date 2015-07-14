package com.fortnox.wh.transactions.v1;

import com.fortnox.auth.Auth;
import rx.Observable;

import javax.ws.rs.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/wh-transactions-v1/inbound-transactions")
public interface InboundTransactionResource {

    @GET
    public Observable<List<InboundTransactionEntity>> list(Auth auth, @QueryParam("inboundIds") String inboundIds);

    @POST
    public Observable<InboundTransactionEntity> create(Auth auth, InboundTransactionEntity inbound);

    @PUT
    @Path("{inboundId}")
    public Observable<InboundTransactionEntity> update(Auth auth, @PathParam("inboundId") String inboundId, InboundTransactionEntity inbound);

    @GET
    @Path("{inboundId}")
    public Observable<InboundTransactionEntity> getTransaction(Auth auth, @PathParam("inboundId") String inboundId);

    @POST
    @Path("approve")
    public Observable<List<InboundTransactionEntity>> approve(Auth auth, List<String> inboundIds);

    @POST
    @Path("bunch")
    public Observable<List<InboundTransactionEntity>> storeBunch(Auth auth, List<Map<String, Object>> inbounds);

    @DELETE
    @Path("{inboundId}")
    public Observable<Integer> deleteTransaction(Auth auth, @PathParam("inboundId") String inboundId);

    @POST
    @Path("delete-bunch")
    public Observable<Integer> deleteBunch(Auth auth, List<String> inboundIds);

}
