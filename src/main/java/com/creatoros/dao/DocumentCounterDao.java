package com.creatoros.dao;

import com.creatoros.enums.DocumentType;

public interface DocumentCounterDao {

    int nextSequence(Long creatorId, DocumentType docType, String financialYear);
}
