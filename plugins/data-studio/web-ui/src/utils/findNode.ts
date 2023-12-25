const levelWeight = {
  root: 1,
  databaseCollect: 2,
  userRoleCollect: 2,
  tablespaceCollect: 2,
  job: 2,
  database: 3,
  user: 3,
  role: 3,
  tablespace: 3,
  schema: 4,
  public: 4,
  person: 4,
  tableCollect: 5,
  terminalCollect: 5,
  viewCollect: 5,
  synonymCollect: 5,
  sequenceCollect: 5,
  table: 6,
  terminal: 6,
  package: 6,
  view: 6,
  synonym: 6,
  sequence: 6,
};

const findNodeById = (node, id, targetType?: keyof typeof levelWeight) => {
  if (Array.isArray(node)) {
    for (let i = 0; i < node.length; i++) {
      const foundNode = findNodeById(node[i], id, targetType);
      if (foundNode) return foundNode;
    }
  } else if (Object.prototype.toString.call(node) === '[object Object]') {
    if (node.id === id) return node;
    if (levelWeight[node.type] > levelWeight[targetType]) return null;
    if (Array.isArray(node.children) && node.children.length) {
      return findNodeById(node.children, id, targetType);
    }
  } else {
    return null;
  }
  return null;
};

const findNodesByType = (node, targetType: keyof typeof levelWeight) => {
  const result = [];
  if (Array.isArray(node)) {
    for (const nodeItem of node) {
      const nodeItemResults = findNodesByType(nodeItem, targetType);
      result.push(...nodeItemResults);
    }
  } else {
    if (
      node.type === targetType ||
      (targetType === 'schema' && ['public', 'person'].includes(node.type))
    ) {
      result.push(node);
    }
    if (levelWeight[node.type] > levelWeight[targetType]) return result;
    if (Array.isArray(node.children) && node.children.length) {
      for (const child of node.children) {
        const childResults = findNodesByType(child, targetType);
        result.push(...childResults);
      }
    }
  }
  return result;
};

export { findNodeById, findNodesByType };
