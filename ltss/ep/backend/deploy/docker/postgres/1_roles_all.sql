--
-- PostgreSQL database cluster dump
--

-- Started on 2018-01-31 10:30:39

\connect postgres

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET escape_string_warning = off;

--
-- Roles
--

CREATE ROLE santa_crm;
ALTER ROLE santa_crm WITH NOSUPERUSER INHERIT NOCREATEROLE NOCREATEDB LOGIN PASSWORD 'SantaCRM.396';

-- Completed on 2018-01-31 10:30:40

--
-- PostgreSQL database cluster dump complete
--

