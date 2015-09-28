# If BB_NO_NETWORK=1, this class restores saved bitbake URI headrevs.
#
# This is useful to support BB_NO_NETWORK with AUTOREV, by letting someone
# ship dumped headrevs to the user, who then inherit this class, which ensures
# that the cached headrevs are used, and upstream is not contacted at parse
# time. BB_SRCREV_POLICY will be set to "cache" as well, if it's not already
# set, as otherwise bitbake will contact upstream to update the cached values.
#
# See also lib/oe/headrevs.py.
#
# This only handles restoring dumped headrevs. Using the python module to dump
# to this database will likely be done at release time, elsewhere.

HEADREVS_RESTORE_STAMP ?= "${PERSISTENT_DIR}/headrevs_restored"
DUMPED_HEADREVS_DB ?= "${COREBASE}/headrevs.db"

python restore_dumped_headrevs() {
    dump_db_path = d.getVar('DUMPED_HEADREVS_DB', True)
    if os.path.exists(dump_db_path) and d.getVar('BB_NO_NETWORK', True) == '1':
        if 'BB_SRCREV_POLICY' not in d:
            d.setVar('BB_SRCREV_POLICY', 'cache')

        stamp_path = d.getVar('HEADREVS_RESTORE_STAMP', True)
        if (not os.path.exists(stamp_path) or
                os.path.getmtime(dump_db_path) > os.path.getmtime(stamp_path)):
            import oe.headrevs
            oe.headrevs.restore_headrevs(d, dump_db_path)
            bb.utils.mkdirhier(os.path.dirname(stamp_path))
            open(stamp_path, 'w').close()
}
restore_dumped_headrevs[eventmask] = "bb.event.ConfigParsed"
addhandler restore_dumped_headrevs
