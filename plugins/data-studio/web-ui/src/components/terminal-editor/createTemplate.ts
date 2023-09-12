import { useRoute } from 'vue-router';
export default async function (): Promise<string> {
  const route = useRoute();
  if (route.name === 'createDebug') {
    switch (route.query.type) {
      case 'function':
        return `CREATE OR REPLACE FUNCTION scott.test(i integer, OUT result bigint)
 RETURNS SETOF integer
 LANGUAGE plpgsql
 NOT FENCED NOT SHIPPABLE
AS $$
DECLARE


BEGIN
  result = i + 1;
  result = result + 2;
  result = result + 3;
  result = result + 4;
  result = result + 5;
  result = result + 6;
  result = result + 7;
  result = result + 8;
  result = result + 9;
  result = result + 10;
  result = result + 11;

RETURN NEXT;
END;$$;
/
`;
      case 'procedure': //CREATE procedure
        return `CREATE [OR REPLACE] PROCEDURE db4ai.procedure_name ([ parameter [IN|OUT|INOUT] datatype[,parameter [IN|OUT|INOUT] datatype] ])

IS  |  AS
DECLARE
	/*declaration_section*/
BEGIN
	/*executable_section*/

END;
/
`;
      case 'sql': //CREATE sql
        return `CREATE [OR REPLACE] FUNCTION db4ai.function_name ([ parameter datatype[,parameter datatype] ])
  RETURNS return_datatype
  LANGUAGE SQL

AS $$
  /*executable_section*/
$$
/
`;
      case 'package':
        return `create package test_pkg_a
IS
  PROCEDURE p1;
  PROCEDURE p2;
end test_pkg_a;
/

create package body test_pkg_a
IS
  g number;
  PROCEDURE p1
  IS
  BEGIN
    g :=1;
    g :=2;
    p2;
  end;
  PROCEDURE p2
  IS
  BEGIN
    g :=3;
    g :=4;
  end;
end test_pkg_a;
/
`;
      case 'anonymous': //anoymous block
        return `DO
$$
DECLARE
/*declaration_section*/
BEGIN
/*executable_section*/
END
$$
LANGUAGE plpgsql;
`;
      default:
        return ``;
    }
  } else {
    return '';
  }
}
