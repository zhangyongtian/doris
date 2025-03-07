# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# where to put generated libraries
set(LIBRARY_OUTPUT_PATH "${BUILD_DIR}/src/vec")
# where to put generated binaries
set(EXECUTABLE_OUTPUT_PATH "${BUILD_DIR}/src/vec")

set(VEC_FILES
  aggregate_functions/aggregate_function_sequence_match.cpp
  aggregate_functions/aggregate_function_retention.cpp
  aggregate_functions/aggregate_function_window_funnel.cpp
  aggregate_functions/aggregate_function_avg.cpp
  aggregate_functions/aggregate_function_collect.cpp
  aggregate_functions/aggregate_function_count.cpp
  aggregate_functions/aggregate_function_distinct.cpp
  aggregate_functions/aggregate_function_sum.cpp
  aggregate_functions/aggregate_function_sort.cpp
  aggregate_functions/aggregate_function_min_max.cpp
  aggregate_functions/aggregate_function_min_by.cpp
  aggregate_functions/aggregate_function_max_by.cpp
  aggregate_functions/aggregate_function_uniq.cpp
  aggregate_functions/aggregate_function_hll_union_agg.cpp
  aggregate_functions/aggregate_function_bit.cpp
  aggregate_functions/aggregate_function_bitmap.cpp
  aggregate_functions/aggregate_function_reader.cpp
  aggregate_functions/aggregate_function_window.cpp
  aggregate_functions/aggregate_function_stddev.cpp
  aggregate_functions/aggregate_function_topn.cpp
  aggregate_functions/aggregate_function_approx_count_distinct.cpp
  aggregate_functions/aggregate_function_group_concat.cpp
  aggregate_functions/aggregate_function_percentile_approx.cpp
  aggregate_functions/aggregate_function_simple_factory.cpp
  aggregate_functions/aggregate_function_orthogonal_bitmap.cpp
  aggregate_functions/aggregate_function_avg_weighted.cpp
  aggregate_functions/aggregate_function_histogram.cpp
  aggregate_functions/aggregate_function_quantile_state.cpp
  columns/column.cpp
  columns/column_array.cpp
  columns/column_struct.cpp
  columns/column_const.cpp
  columns/column_decimal.cpp
  columns/column_nullable.cpp
  columns/column_string.cpp
  columns/column_vector.cpp
  columns/column_map.cpp
  columns/columns_common.cpp
  columns/column_object.cpp
  json/json_parser.cpp
  json/parse2column.cpp
  json/path_in_data.cpp
  common/schema_util.cpp
  common/demangle.cpp
  common/mremap.cpp
  common/pod_array.cpp
  common/string_ref.cpp
  common/sort/heap_sorter.cpp
  common/sort/sorter.cpp
  common/sort/topn_sorter.cpp
  common/sort/vsort_exec_exprs.cpp
  common/string_utils/string_utils.cpp
  common/hex.cpp
  common/allocator.cpp
  core/block.cpp
  core/block_spill_reader.cpp
  core/block_spill_writer.cpp
  core/column_with_type_and_name.cpp
  core/field.cpp
  core/field.cpp
  core/sort_block.cpp
  core/materialize_block.cpp
  data_types/serde/data_type_serde.cpp
  data_types/serde/data_type_map_serde.cpp
  data_types/serde/data_type_array_serde.cpp
  data_types/serde/data_type_struct_serde.cpp
  data_types/serde/data_type_number_serde.cpp
  data_types/serde/data_type_string_serde.cpp
  data_types/serde/data_type_decimal_serde.cpp
  data_types/serde/data_type_object_serde.cpp
  data_types/serde/data_type_fixedlengthobject_serde.cpp
  data_types/serde/data_type_hll_serde.cpp
  data_types/serde/data_type_bitmap_serde.cpp
  data_types/serde/data_type_quantilestate_serde.cpp
  data_types/serde/data_type_nullable_serde.cpp
  data_types/data_type.cpp
  data_types/data_type_array.cpp
  data_types/data_type_struct.cpp
  data_types/data_type_bitmap.cpp
  data_types/data_type_factory.cpp
  data_types/data_type_fixed_length_object.cpp
  data_types/data_type_hll.cpp
  data_types/data_type_nothing.cpp
  data_types/data_type_nothing.cpp
  data_types/data_type_nullable.cpp
  data_types/data_type_nullable.cpp
  data_types/data_type_number_base.cpp
  data_types/data_type_string.cpp
  data_types/data_type_decimal.cpp
  data_types/data_type_map.cpp
  data_types/data_type_quantilestate.cpp
  data_types/get_least_supertype.cpp
  data_types/convert_field_to_type.cpp
  data_types/nested_utils.cpp
  data_types/data_type_date.cpp
  data_types/data_type_date_time.cpp
  data_types/data_type_time_v2.cpp
  data_types/data_type_jsonb.cpp
  data_types/data_type_time.cpp
  data_types/data_type_object.cpp
  exec/vaggregation_node.cpp
  exec/varrow_scanner.cpp
  exec/vsort_node.cpp
  exec/vexchange_node.cpp
  exec/vset_operation_node.cpp
  exec/vunion_node.cpp
  exec/vselect_node.cpp
  exec/vschema_scan_node.cpp
  exec/vempty_set_node.cpp
  exec/vanalytic_eval_node.cpp
  exec/vassert_num_rows_node.cpp
  exec/vrepeat_node.cpp
  exec/vtable_function_node.cpp
  exec/vjdbc_connector.cpp
  exec/vparquet_scanner.cpp
  exec/join/vhash_join_node.cpp
  exec/join/vjoin_node_base.cpp
  exec/join/vnested_loop_join_node.cpp
  exec/join/inner_join_impl.cpp
  exec/join/left_semi_join_impl.cpp
  exec/join/left_anti_join_impl.cpp
  exec/join/left_outer_join_impl.cpp
  exec/join/full_outer_join_impl.cpp
  exec/join/right_outer_join_impl.cpp
  exec/join/cross_join_impl.cpp
  exec/join/right_semi_join_impl.cpp
  exec/join/right_anti_join_impl.cpp
  exec/join/null_aware_left_anti_join_impl.cpp
  exec/data_gen_functions/vnumbers_tvf.cpp
  exec/vdata_gen_scan_node.cpp
  exprs/vectorized_agg_fn.cpp
  exprs/vectorized_fn_call.cpp
  exprs/vexpr.cpp
  exprs/vexpr_context.cpp
  exprs/vliteral.cpp
  exprs/varray_literal.cpp
  exprs/vmap_literal.cpp
  exprs/vstruct_literal.cpp
  exprs/vin_predicate.cpp
  exprs/vbloom_predicate.cpp
  exprs/vbitmap_predicate.cpp
  exprs/vruntimefilter_wrapper.cpp
  exprs/vtuple_is_null_predicate.cpp
  exprs/vslot_ref.cpp
  exprs/vcast_expr.cpp
  exprs/vcase_expr.cpp
  exprs/vinfo_func.cpp
  exprs/vschema_change_expr.cpp
  exprs/table_function/table_function_factory.cpp
  exprs/table_function/vexplode.cpp
  exprs/table_function/vexplode_split.cpp
  exprs/table_function/vexplode_numbers.cpp
  exprs/table_function/vexplode_bitmap.cpp
  exprs/lambda_function/varray_map_function.cpp
  exprs/lambda_function/varray_filter_function.cpp
  functions/array/function_array_index.cpp
  functions/array/function_array_element.cpp
  functions/array/function_array_register.cpp
  functions/array/function_array_size.cpp
  functions/array/function_array_aggregation.cpp
  functions/array/function_array_sort.cpp
  functions/array/function_array_sortby.cpp
  functions/array/function_array_utils.cpp
  functions/array/function_array_shuffle.cpp
  functions/array/function_arrays_overlap.cpp
  functions/array/function_array_distinct.cpp
  functions/array/function_array_remove.cpp
  functions/array/function_array_union.cpp
  functions/array/function_array_except.cpp
  functions/array/function_array_exists.cpp
  functions/array/function_array_intersect.cpp
  functions/array/function_array_slice.cpp
  functions/array/function_array_difference.cpp
  functions/array/function_array_enumerate.cpp
  functions/array/function_array_enumerate_uniq.cpp
  functions/array/function_array_range.cpp
  functions/array/function_array_compact.cpp
  functions/array/function_array_popback.cpp
  functions/array/function_array_popfront.cpp
  functions/array/function_array_constructor.cpp
  functions/array/function_array_with_constant.cpp
  functions/array/function_array_apply.cpp
  functions/array/function_array_concat.cpp
  functions/array/function_array_zip.cpp
  functions/array/function_array_pushfront.cpp
  functions/array/function_array_first_index.cpp
  functions/array/function_array_cum_sum.cpp
  functions/function_map.cpp
  functions/function_struct.cpp
  exprs/table_function/vexplode_json_array.cpp
  functions/math.cpp
  functions/function_bitmap.cpp
  functions/function_bitmap_variadic.cpp
  functions/comparison.cpp
  functions/comparison_less.cpp
  functions/comparison_equals.cpp
  functions/comparison_greater.cpp
  functions/function.cpp
  functions/function_helpers.cpp
  functions/function_hash.cpp
  functions/functions_logical.cpp
  functions/function_case.cpp
  functions/function_cast.cpp
  functions/function_conv.cpp
  functions/function_encryption.cpp
  functions/function_regexp.cpp
  functions/function_hex.cpp
  functions/function_string.cpp
  functions/function_timestamp.cpp
  functions/function_utility.cpp
  functions/comparison_equal_for_null.cpp
  functions/function_json.cpp
  functions/function_jsonb.cpp
  functions/function_datetime_floor_ceil.cpp
  functions/functions_geo.cpp
  functions/hll_cardinality.cpp
  functions/hll_empty.cpp
  functions/hll_hash.cpp
  functions/plus.cpp
  functions/modulo.cpp
  functions/multiply.cpp
  functions/minus.cpp
  functions/int_div.cpp
  functions/divide.cpp
  functions/function_bit.cpp
  functions/is_null.cpp
  functions/is_not_null.cpp
  functions/in.cpp
  functions/like.cpp
  functions/to_time_function.cpp
  functions/time_of_function.cpp
  functions/if.cpp
  functions/function_ifnull.cpp
  functions/nullif.cpp
  functions/random.cpp
  functions/uuid.cpp
  functions/function_coalesce.cpp
  functions/function_date_or_datetime_computation.cpp
  functions/function_date_or_datetime_computation_v2.cpp
  functions/function_date_or_datetime_to_string.cpp
  functions/function_datetime_string_to_string.cpp
  functions/function_grouping.cpp
  functions/function_java_udf.cpp
  functions/function_rpc.cpp
  functions/function_convert_tz.cpp
  functions/function_nonnullable.cpp
  functions/least_greast.cpp
  functions/function_fake.cpp
  functions/url/function_url.cpp
  functions/functions_multi_string_position.cpp
  functions/functions_multi_string_search.cpp
  functions/function_running_difference.cpp
  functions/function_width_bucket.cpp
  functions/match.cpp
  functions/function_quantile_state.cpp
  
  jsonb/serialize.cpp
  olap/vgeneric_iterators.cpp
  olap/vcollect_iterator.cpp
  olap/block_reader.cpp
  olap/olap_data_convertor.cpp
  olap/vertical_merge_iterator.cpp
  olap/vertical_block_reader.cpp
  sink/vmysql_result_writer.cpp
  sink/vresult_sink.cpp
  sink/vdata_stream_sender.cpp
  sink/vtablet_sink.cpp
  sink/vmemory_scratch_sink.cpp
  sink/vmysql_table_writer.cpp
  sink/vmysql_table_sink.cpp
  sink/vodbc_table_sink.cpp
  sink/vresult_file_sink.cpp
  sink/vjdbc_table_sink.cpp
  sink/vtable_sink.cpp
  runtime/vdatetime_value.cpp
  runtime/vdata_stream_recvr.cpp
  runtime/vdata_stream_mgr.cpp
  runtime/vfile_result_writer.cpp
  runtime/vparquet_writer.cpp
  runtime/vorc_writer.cpp
  runtime/vsorted_run_merger.cpp
  runtime/shared_hash_table_controller.cpp
  utils/arrow_column_to_doris_column.cpp
  exec/format/parquet/vparquet_column_chunk_reader.cpp
  exec/format/parquet/vparquet_group_reader.cpp
  exec/format/parquet/vparquet_page_index.cpp
  exec/format/parquet/vparquet_reader.cpp
  exec/format/parquet/vparquet_file_metadata.cpp
  exec/format/parquet/vparquet_page_reader.cpp
  exec/format/parquet/schema_desc.cpp
  exec/format/parquet/vparquet_column_reader.cpp
  exec/format/parquet/level_decoder.cpp
  exec/format/parquet/decoder.cpp
  exec/format/parquet/fix_length_plain_decoder.cpp
  exec/format/parquet/byte_array_plain_decoder.cpp
  exec/format/parquet/byte_array_dict_decoder.cpp
  exec/format/parquet/bool_plain_decoder.cpp
  exec/format/parquet/parquet_common.cpp
  exec/scan/vscan_node.cpp
  exec/scan/vscanner.cpp
  exec/scan/scanner_context.cpp
  exec/scan/scanner_scheduler.cpp
  exec/scan/new_olap_scan_node.cpp
  exec/scan/new_olap_scanner.cpp
  exec/scan/new_file_scan_node.cpp
  exec/scan/vfile_scanner.cpp
  exec/scan/new_odbc_scanner.cpp
  exec/scan/new_odbc_scan_node.cpp
  exec/scan/new_jdbc_scanner.cpp
  exec/scan/new_jdbc_scan_node.cpp
  exec/scan/new_es_scanner.cpp
  exec/scan/new_es_scan_node.cpp
  exec/scan/vmeta_scan_node.cpp
  exec/scan/vmeta_scanner.cpp
  exec/format/csv/csv_reader.cpp
  exec/format/orc/vorc_reader.cpp
  exec/format/json/new_json_reader.cpp
  exec/format/table/table_format_reader.cpp
  exec/format/table/iceberg_reader.cpp
  exec/format/file_reader/new_plain_text_line_reader.cpp
  exec/format/file_reader/new_plain_binary_line_reader.cpp
  exec/format/parquet/delta_bit_pack_decoder.cpp
  exec/format/parquet/bool_rle_decoder.cpp
  exec/jni_connector.cpp
  exec/scan/jni_reader.cpp
  )

if (WITH_MYSQL)
    set(VEC_FILES
            ${VEC_FILES}
            exec/vmysql_scan_node.cpp
            exec/scan/mysql_scanner.cpp)
endif ()

add_library(Vec STATIC
        ${VEC_FILES}
        )
