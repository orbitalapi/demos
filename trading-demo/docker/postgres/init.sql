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
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(5, 'Alex', 'Turner', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(6, 'Sarah', 'Mitchell', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(7, 'Ryan', 'Harper', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(8, 'Emily', 'Fischer', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(9, 'Jordan', 'Patel', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(10, 'Megan', 'Carter', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(11, 'Jason', 'Rodriguez', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(12, 'Lauren', 'Brooks', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(13, 'Kevin', 'Mitchell', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(14, 'Emma', 'Thompson', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(15, 'Derek', 'Hayes', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(16, 'Olivia', 'Foster', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(17, 'Brandon', 'Chen', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(18, 'Jessica', 'Kim', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(19, 'Carter', 'Reynolds', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(20, 'Zoe', 'Morgan', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(21, 'Ethan', 'Gallagher', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(22, 'Madison', 'Wright', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(23, 'Mason', 'Davis', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(24, 'Ava', 'Richardson', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(25, 'Nathan', 'Cooper', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(26, 'Chloe', 'Bennett', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(27, 'Tyler', 'Martinez', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(28, 'Lily', 'Adams', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(29, 'Caleb', 'Brooks', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(30, 'Sophia', 'Nelson', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(31, 'Gavin', 'Harrison', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(32, 'Mia', 'Taylor', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(33, 'Evan', 'Parker', 2);
INSERT INTO public.traders(id, "name", lastname, deskid) VALUES(34, 'Aria', 'Reed', 2);


-- Instruments
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('CA62620DAL51', '2031-04-15', 'MUNICIPAL FINANCE AUTHORITY BC', 'MUN FIN AUTH BC/DEB 20310415 UNSEC');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('JP351265BE99', '2024-09-27', 'JAPAN FINANCE ORGANIZATION FOR MUNICIPALITIES', 'JFM/0.574 BD 20240927 SECD');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('USU8810LAA18', '2025-08-15', 'Tesla Inc', 'TSLA 5.3 08/15/25 REGS');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US892331AN94', '2031-03-25', 'Toyota Motor Corp.', 'TOYOTA MOTOR 21/31');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US61746BEG77', '2047-01-20', 'MORGAN STANLEY', 'MORGAN STANLEY/GTD SUB NT SUB SR');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US023135CJ31', '2052-03-14', 'AMAZON COM INC', 'AMAZON COM INC/3.95 NT 20520413 UN');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US06051GGM50', '2038-04-24', 'BANK AMER CORP', 'BK AMER CORP/FXD FR GTD SUB NT');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US94974BGU89', '2046-07-12', 'WELLS FARGO', 'WELLS FARGO & C/4.75 TRA # TR SR');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US459200KC42', '2049-05-15', 'INTERNATIONAL BUSINESS MACHS CORP', 'INTL BUS MACHIN/4.25 NT 20490515 UN');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US666807BP60', '2047-10-15', 'NORTHROP GRUMMAN CORP', 'NORTHROP GRUMMA/4.03 SR SUB NT SR S');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US172967KR13', '2046-05-18', 'CITIGROUP INC', 'CITIGROUP INC/4.75 GTD SUB NT SUB');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US30303M8J41', '2052-08-15', 'META PLATFORMS INC', 'META PLATFORMS /4.45 SR NT 20520815');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US38148YAA64', '2038-10-31', 'GOLDMAN SACHS GROUP INC', 'GOLDMAN SACHS G/FXD FR SR SUB NT');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US084664DB47', '2052-03-15', 'BERKSHIRE HATHAWAY FIN CORP', 'BERKSHIRE HATHA/3.85 SR NT 20520315');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US037833BA77', '2045-02-09', 'APPLE Inc', 'APPLE INC/3.45 GTD SUB NT SUB');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US254687FZ49', '2051-01-13', 'DISNEY WALT CO', 'DISNEY WALT CO/3.6 GTD NT 20510113');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US42824CAY57', '2045-10-15', 'HEWLETT PACKARD ENTERPRISE CO', 'HEWLETT PACKARD/6.35 GTD SUB NT SUB');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US68389XAH89', '2039-08-07', 'ORACLE CORP', 'ORACLE CORP/6.125 NT 20390708 UNSEC');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US023135AQ91', '2044-12-05', 'AMAZON COM INC', 'AMAZON COM INC/4.95 GTD SUB NT SUB');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US594918CD48', '2060-06-01', 'MICROSOFT CORP', 'MICROSOFT CORP/2.675 NT 20600601 ');
INSERT INTO public.instruments (id, maturitydate, issuername, "name") VALUES('US35137LAK17', '2049-01-25', 'FOX CORP', 'FOX CORP/5.576 SR NT SR SR');


