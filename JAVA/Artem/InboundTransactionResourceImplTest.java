package com.fortnox.wh.transactions.v1;

import com.fortnox.Result;
import com.fortnox.auth.Auth;
import com.fortnox.reactivewizard.util.WebException;
import com.fortnox.wh.items.v1.ItemsResource;
import com.fortnox.wh.sequences.v1.SequenceEntity;
import com.fortnox.wh.sequences.v1.Sequence;
import com.fortnox.wh.transactions.dao.InboundTransactionDAO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import rx.Observable;

import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fortnox.wh.items.v1.ItemEntity;
import com.fortnox.wh.items.v1.ItemsResource;

public class InboundTransactionResourceImplTest {

    Auth auth = mock(Auth.class);
    InboundTransactionDAO dao = mock(InboundTransactionDAO.class);
    Sequence sequences = mock(Sequence.class);
    ItemsResource itemsResource = mock(ItemsResource.class);

    @Before
    public void setup() {

        when(auth.getCustomerId()).thenReturn("1");

        Result<ItemEntity> r = new Result<ItemEntity>();
        ItemEntity item = new ItemEntity() {
            {
                setId("1");
                setDescription("Item 1");
            }
        };
        r.setResult(item);
        when(itemsResource.findItemById(Mockito.any(), Mockito.any())).thenReturn(Observable.just(r));
    }

    @Test
    public void testList() {
        // Given
        when(dao.select(Mockito.any())).thenReturn(Observable.from(Arrays.asList(
                new InboundTransactionEntity() {
                    {
                        setDbId(1);
                        setItemId("1");

                    }
                },
                new InboundTransactionEntity() {
                    {
                        setDbId(1);
                        setItemId("2");
                    }
                }
        )));

        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);

        // When
        List<InboundTransactionEntity> list = service.list(auth, null).toBlocking().single();

        // Then
        Mockito.verify(dao).select(Mockito.any());
        Mockito.verify(itemsResource, Mockito.times(2)).findItemById(Mockito.any(), Mockito.any());
        assertThat(list).isNotEmpty();
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).getItemId()).isEqualTo("1");
        assertThat(list.get(1).getItemId()).isEqualTo("2");
    }

    @Test
    public void testListByIds() {
        // Given
        when(dao.findByIds(Mockito.any(), Mockito.any())).thenReturn(Observable.from(Arrays.asList(
                new InboundTransactionEntity() {
                    {
                        setDbId(1);
                        setItemId("1");

                    }
                },
                new InboundTransactionEntity() {
                    {
                        setDbId(1);
                        setItemId("2");
                    }
                }
        )));

        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);

        // When
        List<InboundTransactionEntity> list = service.list(auth, "1,2,3").toBlocking().single();

        // Then
        Mockito.verify(dao).findByIds(Mockito.any(), Mockito.any());
        Mockito.verify(itemsResource, Mockito.times(2)).findItemById(Mockito.any(), Mockito.any());
        assertThat(list).isNotEmpty();
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).getItemId()).isEqualTo("1");
        assertThat(list.get(1).getItemId()).isEqualTo("2");
    }

    @Test
    public void testCreate() {
        // Given
        when(dao.create(Mockito.any())).thenReturn(Observable.just(1));
        when(sequences.getNextNumber(Mockito.any(), Mockito.any())).thenReturn(Observable.just(new SequenceEntity() {
            {
                setNbr(1);
            }
        }));

        InboundTransactionEntity entity = new InboundTransactionEntity() {
            {
                setDbId(1);
                setItemId("1");
            }
        };

        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);

        // When
        InboundTransactionEntity result = service.create(auth, entity).toBlocking().single();

        // Then
        Mockito.verify(dao).create(Mockito.any());
        Mockito.verify(sequences).getNextNumber(Mockito.any(), Mockito.any());
        assertThat(result.getStatus()).isEqualTo(InboundTransactionEntity.Status.PENDING);
        assertThat(result.getInboundId()).isEqualTo("I_000000000001");
    }

    @Test
    public void testGenerateInboundId() {
        // Given
        InboundTransactionEntity entity = new InboundTransactionEntity() {
            {
                setDbId(1);
                setItemId("1");
            }
        };

        // Test
        assertThat(entity.generateInboundId(2)).isEqualTo("I_000000000002");
        assertThat(entity.generateInboundId(201)).isEqualTo("I_000000000201");
        assertThat(entity.generateInboundId(5201)).isEqualTo("I_000000005201");
        assertThat(entity.generateInboundId(85201)).isEqualTo("I_000000085201");
        assertThat(entity.generateInboundId(985201)).isEqualTo("I_000000985201");

    }

    @Test
    public void testApprove() {
        // Given
        when(dao.findByIds(Mockito.any(), Mockito.any())).thenReturn(Observable.from(Arrays.asList(
                new InboundTransactionEntity() {
                    {
                        setDbId(1);
                        setItemId("1");

                    }
                },
                new InboundTransactionEntity() {
                    {
                        setDbId(1);
                        setItemId("2");
                    }
                }
        )));

        when(dao.approve(Mockito.any())).thenReturn(Observable.just(1));

        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);

        // When
        List<InboundTransactionEntity> list = service.approve(auth, new ArrayList<String>() {
            {
                add("1");
                add("2");
                add("3");
            }
        }).toBlocking().single();

        // Then
        Mockito.verify(dao).findByIds(Mockito.any(), Mockito.any());
        Mockito.verify(dao, Mockito.times(2)).approve(Mockito.any());
        assertThat(list).isNotEmpty();
        assertThat(list.size()).isEqualTo(2);
    }

    @Test(expected = WebException.class)
    public void testStoreBunchEmptyData() {
        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);
        service.storeBunch(auth, null);
    }

    @Test
    public void testStoreBunch() {
        // Given
        when(dao.update(Mockito.any())).thenReturn(Observable.just(1));
        when(dao.create(Mockito.any())).thenReturn(Observable.just(1));
        when(sequences.getNextNumber(Mockito.any(), Mockito.any())).thenReturn(Observable.just(new SequenceEntity() {
            {
                setNbr(2);
            }
        }));

        List<Map<String, Object>> bunch = Arrays.asList(
                new HashMap() {
                    {
                        put("inboundId", "I_000000000001");
                        put("itemId", "1");
                        put("numAvailableItems", 10);
                    }
                },
                new HashMap() {
                    {
                        put("itemId", "2");
                        put("numAvailableItems", 5);
                    }
                }
        );

        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);

        // When
        List<InboundTransactionEntity> list = service.storeBunch(auth, bunch).toBlocking().single();

        // Then
        Mockito.verify(dao).update(Mockito.any());
        Mockito.verify(dao).create(Mockito.any());
        Mockito.verify(sequences).getNextNumber(Mockito.any(), Mockito.any());
        assertThat(list).isNotEmpty();
        assertThat(list.size()).isEqualTo(2);
    }

    @Test(expected = WebException.class)
    public void testApproveEmptyIds() {
        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);
        service.approve(auth, null);
    }

    @Test
    public void testUpdate() {
        // Given
        when(dao.update(Mockito.any())).thenReturn(Observable.just(1));

        InboundTransactionEntity entity = new InboundTransactionEntity() {
            {
                setDbId(1);
                setItemId("1");
                setInboundId("I_000000000001");
                setStatus(InboundTransactionEntity.Status.PENDING);
                setNumAvailableItems(10);
            }
        };

        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);

        // When
        InboundTransactionEntity result = service.update(auth, "I_000000000001", entity).toBlocking().single();

        // Then
        Mockito.verify(dao).update(Mockito.any());
        assertThat(result.getStatus()).isEqualTo(InboundTransactionEntity.Status.PENDING);
        assertThat(result.getInboundId()).isEqualTo("I_000000000001");
        assertThat(result.getNumAvailableItems()).isEqualTo(10);
    }

    @Test
    public void testGetTransaction() {
        // Given
        when(dao.findById(Mockito.any(), Mockito.any())).thenReturn(Observable.from(Arrays.asList(
                new InboundTransactionEntity() {
                    {
                        setDbId(1);
                        setItemId("1");
                        setInboundId("I_000000000001");
                        setStatus(InboundTransactionEntity.Status.PENDING);
                        setNumAvailableItems(10);
                    }
                }
        )));

        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);

        // When
        InboundTransactionEntity result = service.getTransaction(auth, "I_000000000001").toBlocking().single();

        // Then
        Mockito.verify(dao).findById(Mockito.any(), Mockito.any());
        Mockito.verify(itemsResource).findItemById(Mockito.any(), Mockito.any());
        assertThat(result.getStatus()).isEqualTo(InboundTransactionEntity.Status.PENDING);
        assertThat(result.getInboundId()).isEqualTo("I_000000000001");
        assertThat(result.getNumAvailableItems()).isEqualTo(10);
    }

    @Test(expected = rx.exceptions.CompositeException.class)
    public void testGetManualDeliveryNotFound() {
        //todo: why is not WebException.class ?
        // Given
        when(dao.findById(Mockito.any(), Mockito.any())).thenReturn(Observable.from(Arrays.asList()));

        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);

        // When
        service.getTransaction(auth, "I_000000000001").toBlocking().single();

        // Then - exception
    }


    @Test
    public void testDeleteTransaction() {
        // Given
        when(dao.deleteById(Mockito.any(), Mockito.any())).thenReturn(Observable.just(1));

        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);

        // When
        service.deleteTransaction(auth, "I_000000000001");

        // Then
        Mockito.verify(dao).deleteById(Mockito.any(), Mockito.any());
    }

    @Test
    public void testDeleteBunch() {
        // Given
        when(dao.deleteById(Mockito.any(), Mockito.any())).thenReturn(Observable.just(1));

        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);

        // When
        service.deleteBunch(auth, new ArrayList<String>() {
            {
                add("I_000000000001");
                add("I_000000000002");
                add("I_000000000003");
            }
        });

        // Then
        Mockito.verify(dao, Mockito.times(3)).deleteById(Mockito.any(), Mockito.any());
    }

    @Test(expected = WebException.class)
    public void testDeleteEmptyBunchException() {
        // Given
        InboundTransactionResource service = new InboundTransactionResourceImpl(dao, sequences, itemsResource);
        // When
        service.deleteBunch(auth, new ArrayList<String>());

        // Then - exception
    }
}
