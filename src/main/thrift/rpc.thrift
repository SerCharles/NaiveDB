namespace java cn.edu.thssdb.rpc.thrift

struct Status {
  1: required i32 code;
  2: optional string msg;
}

struct GetTimeReq {
}

struct ConnectReq{
  1: required string username
  2: required string password
}

struct ConnectResp{
  1: required Status status
  2: required i64 sessionId
}

struct DisconnetReq{
  1: required i64 sessionId
}

struct DisconnetResp{
  1: required Status status
}

struct GetTimeResp {
  1: required string time
  2: required Status status
}

struct ExecuteStatementReq {
  1: required i64 sessionId
  2: required string statement
}

struct ExecuteStatementResp{
  1: required Status status
  2: required bool isAbort
  3: required bool hasResult
  // only for query
  4: optional list<string> columnsList
  5: optional list<list<string>> rowList
}

service IService {
  GetTimeResp getTime(1: GetTimeReq req);
  ConnectResp connect(1: ConnectReq req);
  DisconnetResp disconnect(1: DisconnetResp req);
  ExecuteStatementResp executeStatement(1: ExecuteStatementReq req);
}
