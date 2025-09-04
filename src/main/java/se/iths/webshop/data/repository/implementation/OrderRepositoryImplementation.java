package se.iths.webshop.data.repository.implementation;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import se.iths.webshop.business.entity.CustomerOrder;
import se.iths.webshop.data.repository.OrderRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class OrderRepositoryImplementation implements OrderRepository {

    @Override
    public Optional<CustomerOrder> findById(long Id) {
        return Optional.empty();
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends CustomerOrder> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends CustomerOrder> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<CustomerOrder> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public CustomerOrder getOne(Long aLong) {
        return null;
    }

    @Override
    public CustomerOrder getById(Long aLong) {
        return null;
    }

    @Override
    public CustomerOrder getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends CustomerOrder> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends CustomerOrder> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends CustomerOrder> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends CustomerOrder> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends CustomerOrder> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends CustomerOrder> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends CustomerOrder, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends CustomerOrder> S save(S entity) {
        return null;
    }

    @Override
    public <S extends CustomerOrder> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<CustomerOrder> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<CustomerOrder> findAll() {
        return null;
    }

    @Override
    public List<CustomerOrder> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(CustomerOrder entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends CustomerOrder> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<CustomerOrder> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<CustomerOrder> findAll(Pageable pageable) {
        return null;
    }
}
