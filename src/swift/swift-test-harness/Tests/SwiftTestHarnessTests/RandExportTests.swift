import Testing
import Rand

@Suite("Rand Swift Export Tests")
struct RandExportTests {
    @Test("StepRng yields deterministic words and little-endian byte fills")
    func stepRngDeterministicWordsAndBytes() {
        let step = StepRng(initial: 0x0102_0304_0506_0708, increment: 5)
        #expect(step.nextU64() == 0x0102_0304_0506_0708)
        #expect(step.nextU64() == 0x0102_0304_0506_070D)
        #expect(step.nextU32() == 0x0506_0712)
    }

    @Test("Rng boolean, ratio, and floating-point sampling obey boundary invariants")
    func rngBooleanRatioAndStandardUniform() {
        let rng = StepRng(initial: 0, increment: 1)
        #expect(rng.randomBool(p: 0.0) == false)
        #expect(rng.randomBool(p: 1.0) == true)
        #expect(rng.genBool(p: 0.0) == false)
        #expect(rng.genBool(p: 1.0) == true)
        #expect(rng.randomRatio(numerator: 0, denominator: 10) == false)
        #expect(rng.randomRatio(numerator: 10, denominator: 10) == true)
        #expect(rng.genRatio(numerator: 0, denominator: 7) == false)
        #expect(rng.genRatio(numerator: 7, denominator: 7) == true)

        let zeroSample = StepRng(initial: 0, increment: 0).random()
        #expect(zeroSample == 0.0)
        let aliasSample = StepRng(initial: 0, increment: 0).gen()
        #expect(aliasSample == 0.0)
    }

    @Test("StandardUniform and Alphanumeric sample expected domains")
    func distributionsSampleExpectedDomains() {
        let rng = RandomRng()
        let unit = distr.StandardUniform.shared.sample(rng: rng)
        #expect(unit >= 0.0 && unit < 1.0)

        let ranged = rng.randomRange(low: 2.0, highExclusive: 5.0)
        #expect(ranged >= 2.0 && ranged < 5.0)
        let genRanged = rng.genRange(low: 2.0, highExclusive: 5.0)
        #expect(genRanged >= 2.0 && genRanged < 5.0)

        let str = distr.Alphanumeric.shared.sampleString(rng: rng, len: 16)
        #expect(str.count == 16)
        #expect(str.allSatisfy { $0.isASCII && ($0.isLetter || $0.isNumber) })
    }
}
