-- Check Invariants
-------------------


-- Detect non official invariant (invariant that are neither private or public) --
select distinct a.idname, b.val from invariant a
left outer join
(select distinct `value` val from invariant where idname in ('INVARIANTPRIVATE' , 'INVARIANTPUBLIC')) as b
on b.val = a.idname
where b.val is null;


-- Detect invariants that are at the same time private and public... --
select distinct a.`value`, b.`value` from invariant a
join
(select distinct `value` from invariant where idname in ('INVARIANTPUBLIC')) as b
on b.`value` = a.`value`
where idname in ('INVARIANTPRIVATE');


