# \# THREATS.md

# 

# | Threat | How I stop it | Test that proves it |

# |---|---|---|

# | Same request sent twice pays twice | Idempotency key | (to do) |

# | Two withdrawals at once overdraw an account | Row locking | (to do) |

# | User A reads user B's account | Ownership check | (to do) |

# | Audit log edited or deleted | Separate owner user, app user has INSERT/SELECT only, triggers block UPDATE, DELETE and TRUNCATE | Verified manually with psql as app user (DELETE, TRUNCATE, DROP TRIGGER all fail). Automated test to add. |

