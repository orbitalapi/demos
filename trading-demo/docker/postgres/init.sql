CREATE DATABASE orbital;

CREATE TABLE public.desks (
	id int4 NOT NULL,
	name varchar NULL,
	CONSTRAINT desks_pk PRIMARY KEY (id)
);


CREATE TABLE public.traders (
	id int4 NOT NULL,
	"name" varchar NULL,
	lastname varchar NULL,
	deskid int4 NULL,
	CONSTRAINT traders_pk PRIMARY KEY (id)
);

CREATE TABLE public.instruments (
	id varchar NOT NULL,
	maturitydate date NOT NULL,
	issuername varchar NOT NULL,
	"name" varchar NULL,
	CONSTRAINT instruments_pk PRIMARY KEY (id)
);

-- Desks
INSERT INTO public.desks(id, "name") VALUES(1, 'Municipal Bonds Desk');
INSERT INTO public.desks(id, "name") VALUES(2, 'Corporate Bonds Desk');
-- Traders
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(1, 'Curtis', 'Quirke', 1);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(2, 'Christine', 'Macintosh', 1);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(3, 'Isabelle', 'Haines', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(4, 'Eric', 'Chapel', 2);
-- Instruments
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('CA62620DAL51', '2031-04-15', 'MUNICIPAL FINANCE AUTHORITY BC', 'MUN FIN AUTH BC/DEB 20310415 UNSEC');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('JP351265BE99', '2024-09-27', 'JAPAN FINANCE ORGANIZATION FOR MUNICIPALITIES', 'JFM/0.574 BD 20240927 SECD');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('USU8810LAA18', '2025-08-15', 'Tesla Inc', 'TSLA 5.3 08/15/25 REGS');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US892331AN94', '2031-03-25', 'Toyota Motor Corp.', 'TOYOTA MOTOR 21/31');

