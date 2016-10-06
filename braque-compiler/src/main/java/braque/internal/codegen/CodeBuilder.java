package braque.internal.codegen;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;

/**
 * A pimped-out StringBuilder userful for building code.
 * Created by mikesolomon on 18/09/16.
 */

public class CodeBuilder {
    final private StringBuilder mBuilder = new StringBuilder();
    private int mLevel = 0;
    private final int mIndent;
    CodeBuilder() {
        this(2, 0);
    }
    CodeBuilder(int indent, int level) {
        mIndent = indent;
        mLevel = level;
    }
    private Map<String, String> mTags = new HashMap<>();
    private String space() {
        String out = "";
        for (int i = 0; i < mIndent * mLevel; i++) {
            out += " ";
        }
        return out;
    }
    CodeBuilder dadd() {
        return d().add();
    }
    CodeBuilder add(String s) {
        return a("add").a(s).P();
    }
    CodeBuilder get(String s) {
        return a("get").a(s).Pp();
    }
    CodeBuilder get(TypeElement tp) {
        return a("get").a(tp.getSimpleName().toString()).Pp();
    }
    CodeBuilder set(String s) {
        return a("set").a(s).P();
    }
    CodeBuilder set(TypeElement tp) {
        return set(tp.getSimpleName().toString());
    }
    CodeBuilder addTo(String s) {
        return add("To"+s);
    }
    CodeBuilder addTo(TypeElement tp) {
        return addTo(tp.getSimpleName().toString());
    }
    CodeBuilder removeFrom(String s) {
        return remove("From"+s);
    }
    CodeBuilder removeFrom(TypeElement tp) {
        return removeFrom(tp.getSimpleName().toString());
    }
    CodeBuilder add() {
        return add("");
    }
    CodeBuilder remove() {
        return remove("");
    }
    CodeBuilder remove(String s) {
        return a("remove").a(s).P();
    }
    CodeBuilder iface() {
        return a("interface").s();
    }
    CodeBuilder pcn() {
        return p().cn();
    }
    CodeBuilder cn() {
        return a(";").n();
    }
    private CodeBuilder c() {
        return a(",");
    }
    CodeBuilder knew() {
        return a("new").s();
    }
    CodeBuilder eq() {
        return a("=").s();
    }
    CodeBuilder nul() {
        return a("null");
    }
    CodeBuilder atag(String s, String tag) {
        mTags.put(tag, s);
        return a(s);
    }
    CodeBuilder stringProv() {
        return braqued().a("StringProvisioner").d();
    }
    CodeBuilder stringProvPath() {
        return stringProv().a("path");
    }
    CodeBuilder statik() {
        return a("static").s();
    }
    CodeBuilder and() {
        return a("&&").s();
    }
    CodeBuilder or() {
        return a("||").s();
    }
    CodeBuilder phinal() {
        return a("final").s();
    }
    CodeBuilder phalse() {
        return a("false").s();
    }
    CodeBuilder pubic() {
        return a("public").s();
    }
    CodeBuilder voi() {
        return a("void").s();
    }
    CodeBuilder protect() {
        return a("protected").s();
    }
    CodeBuilder abstr(boolean isAbstract) {
        return a(isAbstract ? "abstract " : "");
    }
    CodeBuilder klass() {
        return a("class").s();
    }
    private CodeBuilder ret() {
        return a("return").s();
    }
    CodeBuilder pkg(String pack) {
        return a("package ").a(pack).a(";").n().n();
    }
    CodeBuilder a(String s) {
        mBuilder.append(s);
        return this;
    }
    CodeBuilder abraque(TypeElement tp) {
        return a(Utils.braque(tp.getQualifiedName().toString()));
    }
    CodeBuilder a(TypeElement tp) {
        return a(tp.getSimpleName().toString());
    }
    CodeBuilder acs(Collection<String> s) {
        return a(StringUtils.join(s,", "));
    }
    CodeBuilder a_(Collection<String> s) {
        return a(StringUtils.join(s, "_"));
    }
    CodeBuilder acns(Collection<String> s) {
        return a(StringUtils.join(s,",\n  "));
    }
    CodeBuilder cs() {
        return c().s();
    }
    CodeBuilder as(String s) {
        return a(s).s();
    }
    CodeBuilder bool() {
        return a("boolean").s();
    }
    CodeBuilder integ() {
        return a("int ").s();
    }
    CodeBuilder instof() {
        return a("instanceof").s();
    }
    CodeBuilder val() {
        return a("val");
    }
    private CodeBuilder u() {
        return a("_");
    }
    CodeBuilder _val() {
        return u().val();
    }
    CodeBuilder vals() {
        return val().s();
    }
    CodeBuilder d() {
        return a(".");
    }
    CodeBuilder _vals() {
        return u().val().s();
    }
    CodeBuilder iff() {
        return a("if").s().P();
    }
    CodeBuilder oeqq() {
        return d().a("equals").P();
    }
    CodeBuilder eqq() {
        return a("==").s();
    }
    CodeBuilder neq() {
        return a("!=").s();
    }
    CodeBuilder _as(String s) {
        return _a(s).s();
    }
    CodeBuilder _as(TypeElement tp) {
        return _as(tp.getSimpleName().toString());
    }
    CodeBuilder _a_s(String s) {
        return _a_(s).s();
    }
    CodeBuilder _a_(String s) {
        return _a(s).u();
    }
    CodeBuilder __as(String s) {
        return __a(s).s();
    }
    CodeBuilder __a(String s) {
        return u()._a(s);
    }
    CodeBuilder klone() {
        return d().a("klone").Pp();
    }
    CodeBuilder _a(String s) {
        return u().a(s);
    }
    CodeBuilder mas(String s) {
        return ma(s).s();
    }
    CodeBuilder ma(String s) {
        return a("m").a(s);
    }
    CodeBuilder P() {
        return a("(");
    }
    CodeBuilder G() {
        return a("<");
    }
    CodeBuilder q(String s) {
        return a("\"").a(s).a("\"");
    }
    CodeBuilder Gg() {
        return Gg("");
    }
    CodeBuilder Gg(String s) {
        return G().a(s).g();
    }
    CodeBuilder cns() {
        return c().n().s();
    }
    CodeBuilder g() {
        return a(">");
    }
    CodeBuilder s() {
        return a(" ");
    }
    CodeBuilder gs() {
        return g().s();
    }
    CodeBuilder p() {
        return a(")");
    }
    private CodeBuilder ps() {
        return p().s();
    }
    CodeBuilder sas(String s) {
        return s().a(s).s();
    }
    CodeBuilder $as(String s) {
        return $a(s).s();
    }
    CodeBuilder col() {
        return a(":").s();
    }
    CodeBuilder $a(String s) {
        return a("$").a(s);
    }
    CodeBuilder $a(TypeElement tp) {
        return $a(tp.getSimpleName().toString());
    }
    CodeBuilder $as(TypeElement tp) {
        return $a(tp).s();
    }
    CodeBuilder impl() {
        return a("implements").s();
    }
    CodeBuilder ext() {
        return a("extends").s();
    }
    CodeBuilder processing() {
        return a(Utils.BRAQUE).d();
    }
    CodeBuilder braqued() {
        return a(Utils.STATIC_METHOD_CLASSES_PACKAGE).d();
    }
    CodeBuilder beg() {
        mLevel++;
        return a("{").n();
    }
    CodeBuilder sbeg() {
        return s().beg();
    }
    CodeBuilder psbeg() {
        return ps().beg();
    }
    CodeBuilder Ppsbeg() {
        return Pps().beg();
    }
    CodeBuilder sp() {
        return a(space());
    }
    CodeBuilder spend() {
        mLevel--;
        return a(space()).a("}").n();
    }
    CodeBuilder jset(String s) {
        return a("java.util.Set<").a(s).a(">").s();
    }
    CodeBuilder jlist(String s) {
        return a("java.util.List<").a(s).a(">").s();
    }
    CodeBuilder sjlist() {
        return jlist("String");
    }
    CodeBuilder jmap(String s0, String s1) {
        return a("java.util.Map<").a(s0).cs().a(s1).a(">").s();
    }
    CodeBuilder sojmap() {
        return jmap("String","Object");
    }

    CodeBuilder hashset() {
        return hashset("");
    }
    CodeBuilder hashset(String s) {
        return a("new java.util.HashSet<").a(s).a(">()");
    }
    CodeBuilder hashmap() {
        return a("new java.util.HashMap<>()");
    }
    CodeBuilder hashmap(String s1, String s2) {
        return knew().a("java.util.HashMap").G().a(s1).cs().a(s2).g().Pp();
    }
    CodeBuilder arraylist(String s) {
        return a("new java.util.ArrayList<").a(s).a(">()");
    }
    CodeBuilder arraylist() {
        return arraylist("");
    }
    CodeBuilder npe() {
        return a("throw new NullPointerException(");
    }
    CodeBuilder n() {
        return a("\n");
    }
    CodeBuilder nsp() {
        return n().sp();
    }
    CodeBuilder Pp() {
        return Pp("");
    }
    CodeBuilder Pp(String s) {
        return P().a(s).p();
    }
    CodeBuilder pd() {
        return p().d();
    }
    CodeBuilder PPp(String s) {
        return P().Pp(s);
    }
    CodeBuilder Ppp() {
        return Pp().p();
    }
    CodeBuilder Pppp() {
        return Ppp().p();
    }
    CodeBuilder Ppppp() {
        return Pppp().p();
    }
    CodeBuilder pp() {
        return p().p();
    }
    CodeBuilder ppp() {
        return pp().p();
    }
    CodeBuilder PP() {
        return P().P();
    }
    CodeBuilder PPP() {
        return PP().P();
    }
    CodeBuilder spret() {
        return sp().ret();
    }
    private CodeBuilder Pps() {
        return Pp().s();
    }
    CodeBuilder priv() {
        return a("private").s();
    }
    CodeBuilder sup() {
        return a("super").P();
    }
    CodeBuilder four() {
        return a("for").P();
    }
    CodeBuilder str() {
        return as("String");
    }
    CodeBuilder override() {
        return a("@Override").n();
    }
    String toS() {
        return toString();
    }
    @Override
    public String toString() {
        String out = mBuilder.toString();
        for (Map.Entry<String, String> entry : mTags.entrySet()) {
            out = out.replace(entry.getKey(), entry.getValue());
        }
        return out;
    }
    CodeBuilder append(String s) {
        return a(s);
    }
}
