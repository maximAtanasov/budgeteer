--MUST BE RUN AGAINST PR #196, commit 6660f94
ALTER TABLE PUBLIC.BUDGET
    ADD LIMIT BIGINT;

ALTER TABLE PUBLIC.BUDGET
    ADD NOTE CLOB;

CREATE TABLE PUBLIC.TEMPLATE (
	ID BIGINT NOT NULL,
	DESCRIPTION VARCHAR(512),
	ISDEFAULT BOOLEAN,
	NAME VARCHAR(128),
	PROJECT_ID BIGINT,
	"TYPE" INTEGER,
	TEMPLATE BLOB,
	CONSTRAINT SYS_PK_10802 PRIMARY KEY (ID)
) ;
CREATE UNIQUE INDEX SYS_IDX_SYS_PK_10802_10803 ON PUBLIC.TEMPLATE (ID);

CREATE TABLE PUBLIC.FORGOT_PASSWORD_TOKEN (
	ID BIGINT NOT NULL IDENTITY,
	EXPIRY_DATE TIMESTAMP,
	TOKEN VARCHAR(255),
	USER_ID BIGINT NOT NULL,
	CONSTRAINT SYS_PK_10730 PRIMARY KEY (ID),
	CONSTRAINT FKFNIW3PQ63SI1QKSW95D2SGMT9 FOREIGN KEY (USER_ID) REFERENCES PUBLIC.BUDGETEER_USER(ID)
) ;
CREATE INDEX SYS_IDX_FKFNIW3PQ63SI1QKSW95D2SGMT9_10935 ON PUBLIC.FORGOT_PASSWORD_TOKEN (USER_ID) ;
CREATE UNIQUE INDEX SYS_IDX_SYS_PK_10730_10731 ON PUBLIC.FORGOT_PASSWORD_TOKEN (ID) ;

CREATE TABLE PUBLIC.VERIFICATION_TOKEN (
	ID BIGINT NOT NULL IDENTITY,
	EXPIRY_DATE TIMESTAMP,
	TOKEN VARCHAR(255),
	USER_ID BIGINT NOT NULL,
	CONSTRAINT SYS_PK_10810 PRIMARY KEY (ID),
	CONSTRAINT FK9WHWFOEBRH5KXEP2UBKWEY8GJ FOREIGN KEY (USER_ID) REFERENCES PUBLIC.BUDGETEER_USER(ID)
) ;
CREATE INDEX SYS_IDX_FK9WHWFOEBRH5KXEP2UBKWEY8GJ_11042 ON PUBLIC.VERIFICATION_TOKEN (USER_ID) ;
CREATE UNIQUE INDEX SYS_IDX_SYS_PK_10810_10811 ON PUBLIC.VERIFICATION_TOKEN (ID) ;

ALTER TABLE PUBLIC.BUDGETEER_USER
    ADD MAIL VARCHAR(255);

ALTER TABLE PUBLIC.BUDGETEER_USER
	ADD MAIL_VERIFIED BOOLEAN;
