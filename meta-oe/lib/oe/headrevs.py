"""Save and restore the bitbake URI headrevs.

This module provides functions to save and restore the BB_URI_HEADREVS from/to
the persist_data database. The BB_URI_HEADREVS are the mappings between git
refs and the revisions they refer to.

This is useful to fully support BB_NO_NETWORK when AUTOREV is involved, as we
can use the dumped mappings rather than contacting upstream at parse time. See
also restore_headrevs.bbclass.

By default, any existing mapping will not be overwritten, as we assume the
user's mappings are more current than any dumped content.
"""


def copy_persist_domain(d, domain, other_db_path, restore=False, overwrite=False):
    import contextlib

    source_table = bb.persist_data.persist(domain, d)
    dest_table = bb.persist_data.SQLTable(other_db_path, domain)
    if restore:
        source_table, dest_table = dest_table, source_table

    with contextlib.nested(source_table, dest_table):
        if overwrite:
            dest_table.update(source_table)
        else:
            for key in source_table:
                if key not in dest_table:
                    dest_table[key] = source_table[key]


def dump_headrevs(d, dump_db_path):
    copy_persist_domain(d, 'BB_URI_HEADREVS', dump_db_path)


def restore_headrevs(d, dump_db_path):
    copy_persist_domain(d, 'BB_URI_HEADREVS', dump_db_path, restore=True)
