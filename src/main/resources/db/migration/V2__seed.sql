insert into customers (name, phone, email) values
 ('Alice', '+64-21-111111', 'alice@example.com'),
 ('Bob',   '+64-21-222222', 'bob@example.com');

insert into accounts (customer_id, number, currency, balance) values
 (1, 'ALC-0001', 'NZD', 1500.00),
 (1, 'ALC-0002', 'USD',  500.00),
 (2, 'BOB-0001', 'NZD',  200.00);

insert into transactions (account_id, type, amount, currency, reference, counterparty, created_at) values
 (1, 'CREDIT', 1000.00, 'NZD', 'Initial deposit', 'CASH', current_timestamp - 5 day),
 (1, 'DEBIT',   200.00, 'NZD', 'Groceries',       'Countdown', current_timestamp - 3 day),
 (1, 'DEBIT',   100.00, 'NZD', 'Fuel',            'Z Energy', current_timestamp - 1 day);
