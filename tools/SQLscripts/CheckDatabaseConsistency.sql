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




-- Check Documentation Translation status
-----------------------------------------

-- Global Translation status 
SELECT *, 
(todo-en_lab_todo)/todo*100 progression_en,
(todo-fr_lab_todo)/todo*100 progression_fr,
(todo-pt_lab_todo)/todo*100 progression_pt
FROM (
SELECT
 sum(c) todo, sum(nb_trad_done) done ,
 sum(cnt_label_en) en_lab_todo, sum(cnt_desc_en) en_desc_todo, 
 sum(cnt_label_fr) fr_lab_todo, sum(cnt_desc_fr) fr_desc_todo,  
 sum(cnt_label_pt) pt_lab_todo, sum(cnt_desc_pt) pt_desc_todo  
FROM (
SELECT d.DocTable, d.DocField, d.DocValue, count(*) nb_trad_done, 1 c,
  den.DocLabel DocLabel_en, if(isnull(den.DocLabel),1,0) cnt_label_en,
  dfr.DocLabel DocLabel_fr, if(isnull(dfr.DocLabel),1,0) cnt_label_fr,
  dpt.DocLabel DocLabel_pt, if(isnull(dpt.DocLabel),1,0) cnt_label_pt,
  den.DocDesc DocDesc_en, if(isnull(den.DocDesc),1,0) cnt_desc_en,
  dfr.DocDesc DocDesc_fr, if(isnull(dfr.DocDesc),1,0) cnt_desc_fr,
  dpt.DocDesc DocDesc_pt, if(isnull(dpt.DocDesc),1,0) cnt_desc_pt
FROM documentation d
LEFT OUTER JOIN 
  ( SELECT * FROM documentation WHERE Lang='en' ) AS den
  ON den.DocTable=d.DocTable and den.DocField=d.DocField and den.DocValue=d.DocValue
LEFT OUTER JOIN 
  ( SELECT * FROM documentation WHERE Lang='fr' ) AS dfr
  ON dfr.DocTable=d.DocTable and dfr.DocField=d.DocField and dfr.DocValue=d.DocValue
LEFT OUTER JOIN 
  ( SELECT * FROM documentation WHERE Lang='pt' ) AS dpt
  ON dpt.DocTable=d.DocTable and dpt.DocField=d.DocField and dpt.DocValue=d.DocValue
GROUP BY d.DocTable, d.DocField, d.DocValue
) doc_count
) doc_2 ;  

-- Translation status per entry.
SELECT d.DocTable, d.DocField, d.DocValue, count(*) nb_trad,
  den.DocLabel DocLabel_en, dfr.DocLabel DocLabel_fr, dpt.DocLabel DocLabel_pt,
  den.DocDesc DocDesc_en, dfr.DocDesc DocDesc_fr, dpt.DocDesc DocDesc_pt 
FROM documentation d
LEFT OUTER JOIN 
  ( SELECT * FROM documentation WHERE Lang='en' ) AS den
  ON den.DocTable=d.DocTable and den.DocField=d.DocField and den.DocValue=d.DocValue
LEFT OUTER JOIN 
  ( SELECT * FROM documentation WHERE Lang='fr' ) AS dfr
  ON dfr.DocTable=d.DocTable and dfr.DocField=d.DocField and dfr.DocValue=d.DocValue
LEFT OUTER JOIN 
  ( SELECT * FROM documentation WHERE Lang='pt' ) AS dpt
  ON dpt.DocTable=d.DocTable and dpt.DocField=d.DocField and dpt.DocValue=d.DocValue
GROUP BY d.DocTable, d.DocField, d.DocValue;


-- Translation status per Table
SELECT *, 
(todo-en_lab_todo)/todo*100 progression_en,
(todo-fr_lab_todo)/todo*100 progression_fr,
(todo-pt_lab_todo)/todo*100 progression_pt
FROM (
SELECT DocTable,
 sum(c) todo, sum(nb_trad_done) done ,
 sum(cnt_label_en) en_lab_todo, sum(cnt_desc_en) en_desc_todo, 
 sum(cnt_label_fr) fr_lab_todo, sum(cnt_desc_fr) fr_desc_todo,  
 sum(cnt_label_pt) pt_lab_todo, sum(cnt_desc_pt) pt_desc_todo  
FROM (
SELECT d.DocTable, d.DocField, d.DocValue, count(*) nb_trad_done, 1 c,
  den.DocLabel DocLabel_en, if(isnull(den.DocLabel),1,0) cnt_label_en,
  dfr.DocLabel DocLabel_fr, if(isnull(dfr.DocLabel),1,0) cnt_label_fr,
  dpt.DocLabel DocLabel_pt, if(isnull(dpt.DocLabel),1,0) cnt_label_pt,
  den.DocDesc DocDesc_en, if(isnull(den.DocDesc),1,0) cnt_desc_en,
  dfr.DocDesc DocDesc_fr, if(isnull(dfr.DocDesc),1,0) cnt_desc_fr,
  dpt.DocDesc DocDesc_pt, if(isnull(dpt.DocDesc),1,0) cnt_desc_pt
FROM documentation d
LEFT OUTER JOIN 
  ( SELECT * FROM documentation WHERE Lang='en' ) AS den
  ON den.DocTable=d.DocTable and den.DocField=d.DocField and den.DocValue=d.DocValue
LEFT OUTER JOIN 
  ( SELECT * FROM documentation WHERE Lang='fr' ) AS dfr
  ON dfr.DocTable=d.DocTable and dfr.DocField=d.DocField and dfr.DocValue=d.DocValue
LEFT OUTER JOIN 
  ( SELECT * FROM documentation WHERE Lang='pt' ) AS dpt
  ON dpt.DocTable=d.DocTable and dpt.DocField=d.DocField and dpt.DocValue=d.DocValue
GROUP BY d.DocTable, d.DocField, d.DocValue
) doc_count
GROUP BY DocTable
) doc_2 ;  




-- Check Environment consistency
-----------------------------------------

-- control if system application is consistent with env table.
SELECT distinct cep.system, cep.application, a.system from countryenvironmentparameters cep
join application a on cep.application=a.application where cep.system<>a.system;
