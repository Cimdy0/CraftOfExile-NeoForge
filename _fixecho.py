P='src/main/java/com/cimdy/coe/data/codec/skill/helper/state/EchoState.java'
b=open(P,'rb').read()
E=b'\r\n'
old=E.join([
 '     * 驱动本通道。完整通道 = {@code StatIds.ECHO_COUNT} + {@code EFF_ADD_VALUE_ATTRIBUTE_COE_ECHO_COUNT}'.encode('utf-8'),
 '     * + StatGen 登记 + 该词缀，经 LivingAttributeValue mixin -> StatEngine.fold 真读。'.encode('utf-8'),
])
new=E.join([
 '     * <p>stat 通道本身完整：{@code StatIds.ECHO_COUNT} + {@code StatGen} 登记'.encode('utf-8'),
 '     * + {@code EFF_ADD_VALUE_ATTRIBUTE_COE_ECHO_COUNT}（ModifierStatEffect ADD_VALUE/GAIN），'.encode('utf-8'),
 '     * 经 LivingAttributeValue mixin -&gt; {@code StatEngine.fold} 真读。'.encode('utf-8'),
])
assert b.count(old)==1, ('stale block', b.count(old))
b=b.replace(old,new)
# normalize the one stray bare LF back to CRLF
b=b.replace('0。\n     * <p>stat'.encode('utf-8'), '0。'+E+'     * <p>stat'.encode('utf-8'))
open(P,'wb').write(b)
crlf=b.count(E); print('EchoState fixed: stale tail replaced + bareLF normalized -> crlf=%d bareLF=%d' % (crlf, b.count(b'\n')-crlf))
