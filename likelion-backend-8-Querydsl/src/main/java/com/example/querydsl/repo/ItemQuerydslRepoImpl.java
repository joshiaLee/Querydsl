package com.example.querydsl.repo;

import com.example.querydsl.dto.ItemSearchParams;
import com.example.querydsl.entity.Item;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.example.querydsl.entity.QItem.item;

@Slf4j
@RequiredArgsConstructor
public class ItemQuerydslRepoImpl implements ItemQuerydslRepo {
    private final JPAQueryFactory queryFactory;
    @Override
    public List<Item> searchDynamic(ItemSearchParams searchParams) {
        return queryFactory
                .selectFrom(item)
                .where(
                        nameEquals(searchParams.getName()),
                        priceBetween(searchParams.getPriceFloor(), searchParams.getPriceCeil())
                )
                .orderBy(item.price.asc())
                .fetch();
    }

    @Override
    public Page<Item> searchDynamic(ItemSearchParams searchParams, Pageable pageable) {
        List<Item> content = queryFactory
                .selectFrom(item)
                .where(
                        nameEquals(searchParams.getName()),
                        priceBetween(searchParams.getPriceFloor(), searchParams.getPriceCeil())
                )
                .orderBy(item.price.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(item.count())
                .from(item)
                .where(
                        nameEquals(searchParams.getName()),
                        priceBetween(searchParams.getPriceFloor(), searchParams.getPriceCeil())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameEquals(String name) {
        return name == null ? null : item.name.eq(name);
    }

    private BooleanExpression priceBetween(Integer small, Integer big) {
        if (small == null && big == null) return null;
        if (small == null) return priceLoe(big);
        if (big == null) return priceGoe(small);
        return item.price.between(small, big);
    }

    private BooleanExpression priceLoe(Integer value) {
        return value == null ? null : item.price.loe(value);
    }

    private BooleanExpression priceGoe(Integer value) {
        return value == null ? null : item.price.goe(value);
    }
}
