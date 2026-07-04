variable "vpc_id" { type = string }
variable "route_table_ids" { type = list(string) }
variable "private_subnet_ids" { type = list(string) }
variable "endpoint_security_group_id" { type = string }