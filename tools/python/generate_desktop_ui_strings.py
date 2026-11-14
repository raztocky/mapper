#!/usr/bin/env python3
"""
Generate libs/platform/localized_types_map.cpp from the Android English types strings
(android/sdk/src/main/res/values/types_strings.xml).
"""

import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def unescape_android(value):
    # Android resource escapes: \' \" \\ \n \t and @/? prefixes.
    out = []
    i = 0
    while i < len(value):
        c = value[i]
        if c == '\\' and i + 1 < len(value):
            nxt = value[i + 1]
            out.append({'n': '\n', 't': '\t'}.get(nxt, nxt))
            i += 2
            continue
        out.append(c)
        i += 1
    return ''.join(out)


def escape_cpp(value):
    return value.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n').replace('\t', '\\t')


def convert_to_localized_types_cpp(input_file, output_file):
    root = ET.parse(input_file).getroot()
    entries = []
    for node in root.iter('string'):
        key = node.get('name')
        if not key:
            continue
        entries.append((key, unescape_android(''.join(node.itertext()))))

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write('#pragma once\n\n')
        f.write('#include <string>\n')
        f.write('#include "3party/ankerl/unordered_dense.h"\n\n')
        f.write('// This file is generated automatically. Do not edit.\n')
        f.write('// See: tools/python/generate_desktop_ui_strings.py\n')
        f.write('using Type2LocalizedType = ankerl::unordered_dense::map<std::string, std::string>;\n')
        f.write('const Type2LocalizedType g_type2localizedType = {\n')
        for i, (key, value) in enumerate(entries):
            comma = ',' if i < len(entries) - 1 else ''
            f.write(f'  {{"{key}", "{escape_cpp(value)}"}}{comma}\n')
        f.write('};\n')
    print(f"Successfully converted {len(entries)} entries from '{input_file}' to '{output_file}'")


def main():
    input_file = Path('android/sdk/src/main/res/values/types_strings.xml')
    output_file = Path('libs/platform/localized_types_map.cpp')
    convert_to_localized_types_cpp(str(input_file), str(output_file))


if __name__ == '__main__':
    sys.exit(main())
