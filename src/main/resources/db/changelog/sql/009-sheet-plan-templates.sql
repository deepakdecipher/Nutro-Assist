--liquibase formatted sql

-- =============================================================================
-- Template 1 : Indian Weight Loss Plan
-- =============================================================================

--changeset nutro-assist:009-wl-template splitStatements:false
INSERT INTO diet_plan_templates (name, goal, total_days, uploaded_by, created_at)
SELECT 'Indian Weight Loss Plan', 'WEIGHT_LOSS', 7, NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM diet_plan_templates WHERE name = 'Indian Weight Loss Plan')

--changeset nutro-assist:009-wl-days splitStatements:false
INSERT INTO template_days (template_id, day_number)
SELECT dpt.id, gs.d
FROM diet_plan_templates dpt, generate_series(1,7) AS gs(d)
WHERE dpt.name = 'Indian Weight Loss Plan'
AND NOT EXISTS (
    SELECT 1 FROM template_days td
    JOIN diet_plan_templates dpt2 ON td.template_id = dpt2.id
    WHERE dpt2.name = 'Indian Weight Loss Plan' AND td.day_number = gs.d
)

--changeset nutro-assist:009-wl-meals splitStatements:false
INSERT INTO template_meals (day_id, meal_type, meal_name, description, calories, protein_g, carbs_g, fat_g, display_order)
SELECT td.id, v.mtype, v.mname, v.descr, v.cal, v.prot, v.carb, v.fat, v.ord
FROM template_days td
JOIN diet_plan_templates dpt ON td.template_id = dpt.id
JOIN (VALUES
  (1,'BREAKFAST',    'Vegetable Poha',                  'Flattened rice with cumin, curry leaves, onion, and mixed vegetables. Topped with lemon and coriander.',280, 7.0,48.0, 6.0,0),
  (1,'MORNING_SNACK','Apple and Green Tea',             'One medium fresh apple with a cup of plain unsweetened green tea.',                                       95,  0.5,23.0, 0.3,1),
  (1,'LUNCH',        '2 Roti + Masoor Dal + Salad',     'Two whole-wheat rotis with red lentil dal and a fresh cucumber-tomato salad.',                           420,16.0,68.0, 8.0,2),
  (1,'EVENING_SNACK','Roasted Makhana',                 'Lightly salted and roasted lotus seeds - high protein, low calorie snack.',                              110, 4.0,18.0, 2.0,3),
  (1,'DINNER',       'Rice + Palak Dal + Raita',        'Steamed white rice with spinach and lentil curry, served with cucumber-mint raita.',                     470,18.0,80.0, 8.0,4),
  (2,'BREAKFAST',    'Egg White Omelette + Toast',      'Two-egg white omelette with vegetables, served with one slice of whole-wheat toast.',                    260,18.0,24.0, 8.0,0),
  (2,'MORNING_SNACK','Banana',                          'One ripe banana for natural energy and potassium.',                                                        90, 1.0,23.0, 0.3,1),
  (2,'LUNCH',        '3 Roti + Channa Dal + Bhindi',    'Three whole-wheat rotis with split chickpea dal and dry-spiced okra sabzi.',                             430,17.0,68.0, 9.0,2),
  (2,'EVENING_SNACK','Kala Channa Sprout Salad',        'Sprouted black chickpeas with onion, tomato, cucumber, lemon juice, and chaat masala.',                  120, 8.0,18.0, 2.0,3),
  (2,'DINNER',       'Rice + Yellow Dal + Sabzi',       'Steamed rice with yellow moong dal tadka and roasted mixed vegetables.',                                  450,15.0,78.0, 8.0,4),
  (3,'BREAKFAST',    'Masala Oats',                     'Rolled oats with onion, tomato, green chilli, and spices. Garnished with fresh coriander.',               290, 9.0,50.0, 6.0,0),
  (3,'MORNING_SNACK','Orange and Green Tea',            'One fresh orange with unsweetened green tea.',                                                              70, 1.0,15.0, 0.2,1),
  (3,'LUNCH',        '2 Roti + Beans Sabzi + Dal',      'Two rotis with French beans sabzi and a small bowl of dal.',                                              400,14.0,65.0, 8.0,2),
  (3,'EVENING_SNACK','Roasted Peas',                    'Lightly spiced and roasted green peas - a fibre-rich crunchy snack.',                                     100, 6.0,16.0, 1.0,3),
  (3,'DINNER',       'Rice + Palak Channa Dal + Salad', 'Steamed rice with spinach and split chickpea dal, served with a fresh salad.',                           470,18.0,78.0, 8.0,4),
  (4,'BREAKFAST',    'Idli (3 pcs) + Coriander Chutney','Steamed rice-and-lentil idli with fresh coriander-mint chutney.',                                        270, 8.0,52.0, 4.0,0),
  (4,'MORNING_SNACK','Guava',                           'One fresh guava - rich in vitamin C and dietary fibre.',                                                    80, 1.0,18.0, 0.5,1),
  (4,'LUNCH',        '2 Roti + Mixed Veg Sabzi + Chaas','Whole-wheat rotis with a mixed vegetable dry curry, served with spiced buttermilk.',                      410,13.0,66.0, 8.0,2),
  (4,'EVENING_SNACK','Makhana and Green Tea',           'Roasted lotus seeds with a cup of plain green tea.',                                                       120, 4.0,20.0, 2.0,3),
  (4,'DINNER',       'Rice + Channa Dal + Lauki',       'Boiled rice with channa dal cooked with bottle gourd and fresh cucumber tomato salad.',                   470,17.0,80.0, 7.0,4),
  (5,'BREAKFAST',    'Sprout Stuffed Roti (2)',          'Whole-wheat rotis stuffed with spiced mixed sprouts (moong, moth) - high protein breakfast.',             300,11.0,50.0, 7.0,0),
  (5,'MORNING_SNACK','Pomegranate and Green Tea',       'Half cup pomegranate seeds with plain green tea - antioxidant-rich snack.',                                 90, 1.5,20.0, 0.5,1),
  (5,'LUNCH',        '2 Roti + Soya Chilli Capsicum',   'Two rotis with soya chunks stir-fried with capsicum, onion, and Indian spices.',                          400,16.0,60.0, 9.0,2),
  (5,'EVENING_SNACK','Light Bhel Chaat',                'Puffed rice tossed with chopped vegetables, tamarind chutney, and coriander. Low-oil version.',           120, 4.0,22.0, 2.0,3),
  (5,'DINNER',       'Brown Rice + Masoor Dal + Salad', 'Nutty brown rice with red lentil curry and a fresh mixed-vegetable salad.',                               450,17.0,76.0, 7.0,4),
  (6,'BREAKFAST',    'Semolina Upma + Green Tea',        'Semolina sauteed with mustard seeds, curry leaves, vegetables, and cashews.',                            280, 8.0,48.0, 5.0,0),
  (6,'MORNING_SNACK','Papaya',                          'One cup of fresh papaya - excellent for digestion and low in calories.',                                    75, 1.0,18.0, 0.3,1),
  (6,'LUNCH',        'Grilled Chicken + 2 Roti + Salad','Grilled chicken breast with Indian spices, two rotis, and cucumber-tomato salad.',                        420,32.0,38.0,10.0,2),
  (6,'EVENING_SNACK','Roasted Peas and Green Tea',      'Spiced roasted peas with a cup of unsweetened green tea.',                                                 110, 7.0,16.0, 1.0,3),
  (6,'DINNER',       'Rice + Yellow Dal + Bhindi',      'Steamed rice with yellow dal tadka and dry bhindi sabzi.',                                                 470,16.0,82.0, 8.0,4),
  (7,'BREAKFAST',    'Masoor Dal Cheela + Green Chutney','Savoury red-lentil pancakes with coriander-mint chutney - high protein, low fat.',                       270,13.0,38.0, 6.0,0),
  (7,'MORNING_SNACK','Watermelon Slices',               'Fresh chilled watermelon - hydrating and very low calorie.',                                                80, 0.8,20.0, 0.2,1),
  (7,'LUNCH',        '2 Roti + White Choley + Salad',   'Two rotis with white chickpea curry and carrot-lettuce salad.',                                            430,17.0,68.0, 8.0,2),
  (7,'EVENING_SNACK','Sprout Salad with Lemon',         'Mixed sprouts with onion, tomato, cucumber, and fresh lemon.',                                             120, 8.0,18.0, 2.0,3),
  (7,'DINNER',       'Rice + Palak Dal + Beetroot Raita','Boiled rice with spinach lentil curry and creamy beetroot raita.',                                        450,17.0,78.0, 7.0,4)
) AS v(day_num,mtype,mname,descr,cal,prot,carb,fat,ord) ON td.day_number = v.day_num
WHERE dpt.name = 'Indian Weight Loss Plan'
AND NOT EXISTS (
    SELECT 1 FROM template_meals tm WHERE tm.day_id = td.id AND tm.meal_type = v.mtype
)

-- =============================================================================
-- Template 2 : Balanced Indian Nutrition Plan
-- =============================================================================

--changeset nutro-assist:009-bal-template splitStatements:false
INSERT INTO diet_plan_templates (name, goal, total_days, uploaded_by, created_at)
SELECT 'Balanced Indian Nutrition Plan', 'MAINTAIN_WEIGHT', 7, NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM diet_plan_templates WHERE name = 'Balanced Indian Nutrition Plan')

--changeset nutro-assist:009-bal-days splitStatements:false
INSERT INTO template_days (template_id, day_number)
SELECT dpt.id, gs.d
FROM diet_plan_templates dpt, generate_series(1,7) AS gs(d)
WHERE dpt.name = 'Balanced Indian Nutrition Plan'
AND NOT EXISTS (
    SELECT 1 FROM template_days td
    JOIN diet_plan_templates dpt2 ON td.template_id = dpt2.id
    WHERE dpt2.name = 'Balanced Indian Nutrition Plan' AND td.day_number = gs.d
)

--changeset nutro-assist:009-bal-meals splitStatements:false
INSERT INTO template_meals (day_id, meal_type, meal_name, description, calories, protein_g, carbs_g, fat_g, display_order)
SELECT td.id, v.mtype, v.mname, v.descr, v.cal, v.prot, v.carb, v.fat, v.ord
FROM template_days td
JOIN diet_plan_templates dpt ON td.template_id = dpt.id
JOIN (VALUES
  (1,'BREAKFAST',    'Omelette (2 eggs) + Toast + Chai', 'Two-egg omelette with vegetables, two whole-wheat toasts, and one cup of ginger-cardamom chai.',    420,24.0,40.0,16.0,0),
  (1,'MORNING_SNACK','Banana and Soaked Almonds',        'One ripe banana and six overnight-soaked almonds for sustained energy.',                             170, 4.0,28.0, 7.0,1),
  (1,'LUNCH',        '3 Roti + Rajma Curry + Curd',      'Three rotis with kidney bean curry, a bowl of plain curd, and sliced onion-cucumber.',              520,20.0,82.0,10.0,2),
  (1,'EVENING_SNACK','Makhana and Masala Chaas',         'Roasted lotus seeds with a glass of spiced buttermilk.',                                             200, 8.0,32.0, 4.0,3),
  (1,'DINNER',       'Rice + Mix Dal + Paneer Bhurji',   'Steamed rice with mixed dal tadka and low-fat paneer bhurji with capsicum and onion.',               620,28.0,88.0,14.0,4),
  (2,'BREAKFAST',    'Poha + Boiled Egg + Chai',         'Vegetable poha with one boiled egg and a cup of chai.',                                              390,18.0,52.0,10.0,0),
  (2,'MORNING_SNACK','Apple and Walnuts',                'One apple and four walnut halves - fibre plus healthy omega-3 fats.',                                 160, 3.0,27.0, 7.0,1),
  (2,'LUNCH',        '3 Roti + Channa Dal + Palak Sabzi','Three rotis with split chickpea dal and sauteed spinach with garlic.',                               510,19.0,82.0,10.0,2),
  (2,'EVENING_SNACK','Roasted Peas and Chaas',           'Spiced roasted peas with a glass of masala chaas.',                                                  200, 9.0,30.0, 4.0,3),
  (2,'DINNER',       'Brown Rice + Egg Curry + Salad',   'Wholesome brown rice with egg curry in tomato-onion gravy and mixed vegetable salad.',               610,30.0,84.0,14.0,4),
  (3,'BREAKFAST',    'Idli (4 pcs) + Sambar + Chutney',  'Four steamed idlis with lentil-vegetable sambar and coconut chutney.',                              380,14.0,68.0, 6.0,0),
  (3,'MORNING_SNACK','Papaya and Soaked Almonds',        'One cup papaya with eight soaked almonds.',                                                           185, 5.0,26.0, 8.0,1),
  (3,'LUNCH',        '3 Roti + Masoor Dal + Gobi Sabzi', 'Three rotis with red lentil dal and dry cauliflower sabzi.',                                         510,18.0,82.0,10.0,2),
  (3,'EVENING_SNACK','Bhel Chaat and Chaas',             'Light bhel puri with spiced buttermilk.',                                                             210, 6.0,36.0, 3.0,3),
  (3,'DINNER',       'Rice + Yellow Dal + Paneer Curry', 'Boiled rice with moong dal and paneer curry in a light tomato-fenugreek gravy.',                     620,30.0,86.0,14.0,4),
  (4,'BREAKFAST',    'Upma + Hard-Boiled Egg + Chai',    'Semolina upma with cashews and vegetables, one hard-boiled egg, and chai.',                          410,20.0,50.0,12.0,0),
  (4,'MORNING_SNACK','Guava and Soaked Almonds',         'One fresh guava and six soaked almonds.',                                                             150, 3.0,22.0, 7.0,1),
  (4,'LUNCH',        '3 Roti + White Choley + Salad',    'Three rotis with white chickpea curry and fresh salad.',                                              530,20.0,82.0,11.0,2),
  (4,'EVENING_SNACK','Makhana and Masala Chaas',         'Roasted fox nuts with a glass of spiced buttermilk.',                                                 200, 8.0,30.0, 4.0,3),
  (4,'DINNER',       'Rice + Palak Paneer + Dal',        'Boiled rice with spinach-paneer curry and a small bowl of plain dal. Cucumber raita on the side.',   630,28.0,88.0,15.0,4),
  (5,'BREAKFAST',    'Oats Porridge + Fruit + Chai',     'Rolled oats cooked with milk, topped with banana and honey. Chai on the side.',                       420,14.0,66.0,10.0,0),
  (5,'MORNING_SNACK','Orange and Soaked Almonds',        'One orange and six soaked almonds.',                                                                  155, 4.0,24.0, 7.0,1),
  (5,'LUNCH',        '3 Roti + Soya Chunks Curry + Salad','Three rotis with soya chunks in tomato-onion curry. Fresh salad with lemon dressing.',              520,28.0,78.0,11.0,2),
  (5,'EVENING_SNACK','Sprout Chaat and Chaas',           'Mixed sprout chaat with vegetables, lemon, and masala chaas.',                                        210, 9.0,32.0, 3.0,3),
  (5,'DINNER',       'Brown Rice + Egg Bhurji + Dal',    'Brown rice with scrambled egg bhurji (3 eggs) and plain moong dal.',                                  610,34.0,80.0,14.0,4),
  (6,'BREAKFAST',    'Masala Dosa + Sambar',             'One crispy masala dosa with potato filling, served with sambar and coriander chutney.',               440,14.0,72.0,12.0,0),
  (6,'MORNING_SNACK','Pomegranate and Soaked Almonds',   'Half cup pomegranate seeds with six soaked almonds.',                                                  175, 5.0,28.0, 7.0,1),
  (6,'LUNCH',        '3 Roti + Grilled Chicken + Dal',   'Three rotis with grilled chicken tikka and a bowl of dal.',                                           560,38.0,70.0,12.0,2),
  (6,'EVENING_SNACK','Makhana and Masala Chaas',         'Roasted lotus seeds and a glass of masala chaas.',                                                    200, 8.0,30.0, 4.0,3),
  (6,'DINNER',       'Rice + Fish Curry + Salad',        'Steamed rice with light Indian fish curry and fresh vegetable salad.',                                 610,32.0,82.0,14.0,4),
  (7,'BREAKFAST',    'Cheela (2 nos) + Curd + Chai',     'Moong dal pancakes with a small bowl of fresh curd and a cup of chai.',                               390,20.0,50.0,10.0,0),
  (7,'MORNING_SNACK','Banana and Soaked Almonds',        'One ripe banana and six soaked almonds.',                                                              190, 4.0,30.0, 7.0,1),
  (7,'LUNCH',        '3 Roti + Kabuli Chana + Raita',    'Three rotis with whole chickpea masala and beetroot-mint raita.',                                      540,21.0,86.0,11.0,2),
  (7,'EVENING_SNACK','Roasted Peas and Chaas',           'Spiced roasted peas with a glass of masala chaas.',                                                   200, 9.0,30.0, 4.0,3),
  (7,'DINNER',       'Rice + Mutton Keema + Dal + Salad','Steamed rice with minced mutton curry, a bowl of plain dal, and fresh salad.',                        640,38.0,80.0,14.0,4)
) AS v(day_num,mtype,mname,descr,cal,prot,carb,fat,ord) ON td.day_number = v.day_num
WHERE dpt.name = 'Balanced Indian Nutrition Plan'
AND NOT EXISTS (
    SELECT 1 FROM template_meals tm WHERE tm.day_id = td.id AND tm.meal_type = v.mtype
)

-- =============================================================================
-- Template 3 : High Protein Indian Plan
-- =============================================================================

--changeset nutro-assist:009-hp-template splitStatements:false
INSERT INTO diet_plan_templates (name, goal, total_days, uploaded_by, created_at)
SELECT 'High Protein Indian Plan', 'MUSCLE_BUILDING', 7, NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM diet_plan_templates WHERE name = 'High Protein Indian Plan')

--changeset nutro-assist:009-hp-days splitStatements:false
INSERT INTO template_days (template_id, day_number)
SELECT dpt.id, gs.d
FROM diet_plan_templates dpt, generate_series(1,7) AS gs(d)
WHERE dpt.name = 'High Protein Indian Plan'
AND NOT EXISTS (
    SELECT 1 FROM template_days td
    JOIN diet_plan_templates dpt2 ON td.template_id = dpt2.id
    WHERE dpt2.name = 'High Protein Indian Plan' AND td.day_number = gs.d
)

--changeset nutro-assist:009-hp-meals splitStatements:false
INSERT INTO template_meals (day_id, meal_type, meal_name, description, calories, protein_g, carbs_g, fat_g, display_order)
SELECT td.id, v.mtype, v.mname, v.descr, v.cal, v.prot, v.carb, v.fat, v.ord
FROM template_days td
JOIN diet_plan_templates dpt ON td.template_id = dpt.id
JOIN (VALUES
  (1,'BREAKFAST',    'Scrambled Eggs (3) + Toast + OJ', 'Three-egg scramble with capsicum and onion, two whole-wheat toasts, and a small glass of orange juice.',440,28.0,42.0,16.0,0),
  (1,'MORNING_SNACK','Banana Smoothie + Almonds',       'Banana blended with low-fat milk, topped with crushed almonds and a pinch of cinnamon.',               270,12.0,40.0, 8.0,1),
  (1,'LUNCH',        '3 Roti + Grilled Chicken + Dal',  'Three whole-wheat rotis, grilled chicken breast (150g), a bowl of moong dal, and cucumber salad.',     580,48.0,56.0,16.0,2),
  (1,'EVENING_SNACK','Makhana and Chaas',               'High-protein roasted fox nuts and spiced buttermilk.',                                                  200, 8.0,28.0, 4.0,3),
  (1,'DINNER',       'Brown Rice + Egg Curry + Palak Dal','Brown rice with three-egg curry and spinach-lentil dal. Side salad.',                                 720,42.0,88.0,18.0,4),
  (2,'BREAKFAST',    'Paneer Bhurji + Roti + Chai',     'Scrambled low-fat paneer with peppers, onion, and spices. Three rotis and chai.',                       480,30.0,50.0,16.0,0),
  (2,'MORNING_SNACK','Boiled Egg (2) + Banana',         'Two hard-boiled eggs and one banana for a protein-carb combination.',                                   210,14.0,26.0, 7.0,1),
  (2,'LUNCH',        '3 Roti + Soya Keema + Dal',       'Three rotis with soya mince cooked with peas and spices, and a bowl of masoor dal.',                    570,40.0,70.0,14.0,2),
  (2,'EVENING_SNACK','Sprout Chaat and Chaas',          'Protein-rich mixed sprout chaat with vegetables and masala chaas.',                                      230,14.0,32.0, 4.0,3),
  (2,'DINNER',       'Rice + Chicken Curry + Dal + Raita','Boiled rice with chicken curry (150g), yellow dal tadka, and cucumber raita.',                        700,48.0,82.0,16.0,4),
  (3,'BREAKFAST',    'Omelette (3 eggs) + Oats + Chai', 'Three-egg omelette with vegetables, a bowl of oats porridge with milk, and chai.',                      480,32.0,52.0,14.0,0),
  (3,'MORNING_SNACK','Apple and Soaked Almonds',        'One apple and ten soaked almonds.',                                                                      210, 5.0,28.0,10.0,1),
  (3,'LUNCH',        '3 Roti + Fish Curry + Dal',       'Three rotis with spiced fish curry (150g fish) and a bowl of moong dal.',                               570,46.0,60.0,14.0,2),
  (3,'EVENING_SNACK','Makhana and Boiled Egg',          'Roasted makhana with one boiled egg - quick high-protein snack.',                                        210,11.0,18.0, 7.0,3),
  (3,'DINNER',       'Brown Rice + Paneer Curry + Dal', 'Nutty brown rice with paneer tikka masala and a bowl of dal. Side salad and raita.',                    720,40.0,86.0,18.0,4),
  (4,'BREAKFAST',    'Cheela (3 nos) + Curd + Chai',    'Moong dal cheela (protein-rich pancakes) with fresh curd and chai.',                                     470,28.0,54.0,12.0,0),
  (4,'MORNING_SNACK','Banana and Walnuts',              'One banana and eight walnut halves for healthy fats and energy.',                                         250, 5.0,34.0,12.0,1),
  (4,'LUNCH',        '3 Roti + Mutton Curry + Dal',     'Three rotis with lean mutton curry (100g) and a bowl of channa dal.',                                    590,44.0,64.0,16.0,2),
  (4,'EVENING_SNACK','Sprout Chaat and Boiled Egg',     'Kala channa sprout salad with one boiled egg.',                                                          220,16.0,20.0, 8.0,3),
  (4,'DINNER',       'Rice + Chicken Tikka + Dal + Salad','Boiled rice with grilled chicken tikka (150g), moong dal, and a large fresh salad.',                  700,50.0,78.0,16.0,4),
  (5,'BREAKFAST',    'Egg Poha + Boiled Egg + Chai',    'Poha cooked with two scrambled eggs, vegetables, and spices. Extra boiled egg and chai.',                460,28.0,52.0,12.0,0),
  (5,'MORNING_SNACK','Orange and Almonds and Walnuts',  'One orange, six soaked almonds, and four walnut halves.',                                                 230, 6.0,30.0,11.0,1),
  (5,'LUNCH',        '3 Roti + Rajma + Egg Bhurji',     'Three rotis with rajma curry and a side of egg bhurji (2 eggs).',                                        590,36.0,78.0,14.0,2),
  (5,'EVENING_SNACK','Makhana and Masala Chaas',        'Roasted makhana with spiced buttermilk.',                                                                 200, 8.0,28.0, 4.0,3),
  (5,'DINNER',       'Brown Rice + Fish Curry + Dal',   'Brown rice with spiced fish curry (150g) and palak dal. Side salad with raita.',                         720,48.0,84.0,16.0,4),
  (6,'BREAKFAST',    'Sprout Paratha + Curd + Chai',    'Whole-wheat parathas stuffed with spiced mixed sprouts, served with curd and chai.',                     500,22.0,62.0,16.0,0),
  (6,'MORNING_SNACK','Papaya and Almonds and Eggs',     'One cup papaya, six almonds, and two boiled eggs.',                                                       290,16.0,26.0,14.0,1),
  (6,'LUNCH',        '3 Roti + Chicken Curry + Dal',    'Three rotis, chicken curry (150g), and a bowl of masoor dal. Side salad.',                               580,50.0,62.0,14.0,2),
  (6,'EVENING_SNACK','Sprout Salad and Chaas',          'High-protein sprout salad with buttermilk.',                                                              210,12.0,28.0, 4.0,3),
  (6,'DINNER',       'Rice + Paneer Sabzi + Egg Curry', 'Boiled rice with paneer capsicum sabzi and a two-egg curry. Cucumber raita and salad.',                  720,44.0,84.0,18.0,4),
  (7,'BREAKFAST',    'Omelette (3 eggs) + Roti + Chai', 'Three-egg masala omelette with two rotis and masala chai.',                                               500,30.0,50.0,18.0,0),
  (7,'MORNING_SNACK','Banana and Almonds and Walnuts',  'One banana, six almonds, and four walnut halves.',                                                        260, 5.0,36.0,12.0,1),
  (7,'LUNCH',        '3 Roti + Soya Chunks + Dal + Raita','Three rotis with soya chunk masala, a bowl of channa dal, and a small raita.',                         570,44.0,72.0,12.0,2),
  (7,'EVENING_SNACK','Makhana + Boiled Egg + Chaas',    'Roasted makhana, one boiled egg, and a glass of masala chaas.',                                          240,14.0,26.0, 8.0,3),
  (7,'DINNER',       'Brown Rice + Chicken Tikka + Dal','Brown rice, tandoori-style chicken (150g), moong dal, and fresh salad with lemon dressing.',             730,50.0,82.0,18.0,4)
) AS v(day_num,mtype,mname,descr,cal,prot,carb,fat,ord) ON td.day_number = v.day_num
WHERE dpt.name = 'High Protein Indian Plan'
AND NOT EXISTS (
    SELECT 1 FROM template_meals tm WHERE tm.day_id = td.id AND tm.meal_type = v.mtype
)
