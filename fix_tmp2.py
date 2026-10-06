p = 'src/main/java/com/cimdy/coe/data/codec/support/math/EventRefCollector.java'

d = open(p, 'rb').read()
print('before: crlf', d.count(b'\r\n'), 'bare_lf', d.count(b'\n') - d.count(b'\r\n'))

# 1) 去掉注释与 if 之间多出来的空行
stray = (b'        // \xe6\x97\xa0\xe9\x9c\x80\xe8\xa7\xa3\xe5\xbc\x95\xe7\x94\xa8\xe3\x80\x82\r\n'
         b'\r\n'
         b'        if (entry instanceof StatRead sr && sr.ref() != null) {')
fixed = b'        // \xe6\x97\xa0\xe9\x9c\x80\xe8\xa7\xa3\xe5\xbc\x95\xe7\x94\xa8\xe3\x80\x82\r\n' \
        b'        if (entry instanceof StatRead sr && sr.ref() != null) {'
assert d.count(stray) == 1, d.count(stray)
d = d.replace(stray, fixed)

# 2) 归一化所有裸 LF -> CRLF
d = d.replace(b'\r\n', b'\n').replace(b'\n', b'\r\n')

open(p, 'wb').write(d)

d2 = open(p, 'rb').read()
print('after : crlf', d2.count(b'\r\n'), 'bare_lf', d2.count(b'\n') - d2.count(b'\r\n'))
open(p, 'r', encoding='utf-8', newline='').read()
print('utf8 decode OK')